package com.jiagouedu.web.action.front.miaosha;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jiagouedu.common.ReturnMessage;
import com.jiagouedu.core.Constants.ProductType;
import com.jiagouedu.core.Constants.RedisKeyPrefix;
import com.jiagouedu.core.Constants.ZookeeperPathPrefix;
import com.jiagouedu.core.Services;
import com.jiagouedu.core.cache.RedisUtil;
import com.jiagouedu.core.front.SystemManager;
import com.jiagouedu.core.jms.MQProducer;
import com.jiagouedu.core.util.CommonMethod;
import com.jiagouedu.core.util.MD5;
import com.jiagouedu.services.front.account.bean.Account;
import com.jiagouedu.services.front.address.AddressService;
import com.jiagouedu.services.front.address.bean.Address;
import com.jiagouedu.services.front.area.bean.Area;
import com.jiagouedu.services.front.order.OrderService;
import com.jiagouedu.services.front.order.bean.Order;
import com.jiagouedu.services.front.orderdetail.bean.Orderdetail;
import com.jiagouedu.services.front.ordership.bean.Ordership;
import com.jiagouedu.services.front.product.ProductService;
import com.jiagouedu.services.front.product.bean.Product;
import com.jiagouedu.services.manage.spec.SpecService;
import com.jiagouedu.web.action.front.FrontBaseController;
import com.jiagouedu.web.action.front.orders.CartInfo;

/**
 * 秒杀处理模块
 * @author aaron.rao
 *
 */
@Controller("frontMiaoshaAction")
@RequestMapping("miaosha")
public class MiaoshaAction extends FrontBaseController<CartInfo>{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(MiaoshaAction.class);
	@Autowired
	private ProductService productService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private SpecService specService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private MQProducer mqProducer;
	@Autowired
	private ZooKeeper zooKeeper;
	
	//商品售完标记map，多线程操作不能用HashMap
	private static ConcurrentHashMap<String, Boolean> productSoldOutMap = new ConcurrentHashMap<>();
	
	public static ConcurrentHashMap<String, Boolean> getProductSoldOutMap() {
		return productSoldOutMap;
	}
	
	@Override
	public Services<CartInfo> getService() {
		return null;
	}
	
	@PostConstruct
	public void init()  throws Exception{
		Product productParam = new Product();
		productParam.setSpecial(ProductType.PRODUCT_MIAOSHA);
		List<Product> miaoshaProducts = productService.selectList(productParam);
		
		for (Product product : miaoshaProducts) {
			RedisUtil.set(RedisKeyPrefix.PRODUCT_STOCK + "_" + product.getId(), String.valueOf(product.getStock()));
		}
		
	}
	
	/**
	 * 秒杀抢购
	 * @param productId
	 * @param token 秒杀接口访问令牌
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "confirm", method = RequestMethod.POST)
	@ResponseBody
	public ReturnMessage oldMiaosha(String productId) throws Exception{
		if(StringUtils.isEmpty(productId)){
			return ReturnMessage.error("商品id参数为空");
		}
		
		Account account = getLoginAccount();
		if(account==null){
			return ReturnMessage.error("必须登录才能参与秒杀");
		}
		
		//用内存里的商品库存校验可以大大提高性能，相比用redis里的库存来判断减少了与redis的交互次数
		if (productSoldOutMap.get(productId) != null) {
			return ReturnMessage.error("商品已抢完");
		}
		
		//查询生成订单的缓存
		if (RedisUtil.get(CommonMethod.getMiaoshaOrderRedisKey(account.getAccount(), productId)) != null) {
			return ReturnMessage.error("用户已经参与过该商品的秒杀活动，不能重复参与");
		}
		
		//设置排队标记，超时时间根据业务情况决定，类似分布式锁
		if (!RedisUtil.set(CommonMethod.getMiaoshaOrderWaitFlagRedisKey(account.getAccount(), productId), productId, "NX", "EX", 120)) {
			return ReturnMessage.error("排队中，请耐心等待");
		}
		
		Address defaultAddress = getUserDefaultAddress(account);
		if (defaultAddress == null) {
			return ReturnMessage.error("必须先填写用户默认收货地址才能参与秒杀");
        }
		
		ReturnMessage result = deductStockCache(productId);
		if (!result.isSuccess()) {
			return result;
		}
		
		Product productParam = new Product();
		productParam.setId(productId);
		Product product = productService.selectOne(productParam);
		if(product==null){
			return ReturnMessage.error("商品不存在");
		}
		
		ReturnMessage validResult = validMiaoshaTime(product);
		if (!validResult.isSuccess()) { 
			return validResult;
        }
		
		if(product.getStock() <= 0){
			//购买的商品数超出库存数
			return ReturnMessage.error("商品已抢完");
		}
		
		return confirmOrder(product, defaultAddress, account); 
	}

	/**
	 * 缓存库存预减
	 * @param productId
	 * @return
	 * @throws Exception
	 */
	private ReturnMessage deductStockCache(String productId) throws Exception {
		Long stock = RedisUtil.decr(RedisKeyPrefix.PRODUCT_STOCK + "_" + productId);
		if (stock == null) {
			return ReturnMessage.error("商品数据还未准备好");
		}
		if (stock < 0) {
			RedisUtil.incr(RedisKeyPrefix.PRODUCT_STOCK + "_" + productId);
			productSoldOutMap.put(productId, true);
			
			//写zk的商品售完标记true
			if (zooKeeper.exists(ZookeeperPathPrefix.PRODUCT_SOLD_OUT, false) == null) {
				zooKeeper.create(ZookeeperPathPrefix.PRODUCT_SOLD_OUT, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			
			if (zooKeeper.exists(ZookeeperPathPrefix.getZKSoldOutProductPath(productId), true) == null) {
				zooKeeper.create(ZookeeperPathPrefix.getZKSoldOutProductPath(productId), "true".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			
			if ("false".equals(new String(zooKeeper.getData(ZookeeperPathPrefix.getZKSoldOutProductPath(productId), true, new Stat())))) {
				zooKeeper.setData(ZookeeperPathPrefix.getZKSoldOutProductPath(productId), "true".getBytes(), -1);
				//监听zk售完标记节点
				zooKeeper.exists(ZookeeperPathPrefix.getZKSoldOutProductPath(productId), true);
			}
			
			return ReturnMessage.error("商品已抢完");
		}
		return ReturnMessage.success();
	}
	
	/**
	 * 秒杀抢购
	 * @param productId
	 * @param token 秒杀接口访问令牌
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "{token}/confirm", method = RequestMethod.POST)
	@ResponseBody
	public ReturnMessage miaosha(String productId, @PathVariable("token")String token) throws Exception{
		if(StringUtils.isEmpty(productId) || StringUtils.isEmpty(token)){
			return ReturnMessage.error("商品id或访问地址参数为空");
		}
		Account account = getLoginAccount();
		if(account==null){
			return ReturnMessage.error("必须登录才能参与秒杀");
		}
		
		//验证用户token
    	boolean check = checkToken(account, productId, token);
    	if(!check){
    		return ReturnMessage.error("非法请求");
    	}
		
    	//用内存里的商品库存校验可以大大提高性能，相比用redis里的库存来判断减少了与redis的交互次数
		if (productSoldOutMap.get(productId) != null) {
			return ReturnMessage.error("商品已抢完");
        }
		
		//查询生成订单的缓存
		if (RedisUtil.get(CommonMethod.getMiaoshaOrderRedisKey(account.getAccount(), productId)) != null) {
			return ReturnMessage.error("用户已经参与过该商品的秒杀活动，不能重复参与");
		}
		
		//设置排队标记，超时时间根据业务情况决定，类似分布式锁
		if (!RedisUtil.set(CommonMethod.getMiaoshaOrderWaitFlagRedisKey(account.getAccount(), productId), productId, "NX", "EX", 120)) {
			return ReturnMessage.error("排队中，请耐心等待");
		}
		
		Address defaultAddress = getUserDefaultAddress(account);
		if (defaultAddress == null) {
			return ReturnMessage.error("必须先填写用户默认收货地址才能参与秒杀");
        }
		
		ReturnMessage result = deductStockCache(productId);
		if (!result.isSuccess()) {
			return result;
		}
		
		Product productParam = new Product();
		productParam.setId(productId);
		Product product = productService.selectOne(productParam);
		if(product==null){
			return ReturnMessage.error("商品不存在");
		}
		
		ReturnMessage validResult = validMiaoshaTime(product);
		if (!validResult.isSuccess()) { 
			return validResult;
        }
		
		if(product.getStock() <= 0){
			//购买的商品数超出库存数
			return ReturnMessage.error("商品已抢完");
		}
		
		return confirmOrder(product, defaultAddress, account); 
	}

	private Address getUserDefaultAddress(Account account) {
	    Address defaultAddress = RedisUtil.get(RedisKeyPrefix.USER_DEFAULT_ADDRESS + account.getId(), Address.class);
		if (defaultAddress == null) {
			Address address = new Address();
			address.setAccount(account.getAccount());
			address.setIsdefault("y");
			defaultAddress = addressService.selectOne(address);
			if(defaultAddress == null){
				return null;
			}
			RedisUtil.set(RedisKeyPrefix.USER_DEFAULT_ADDRESS + account.getId(), defaultAddress);
        }
	    return defaultAddress;
    }
	
	/**
	 * 提交订单
	 * @param product
	 * @param selectAddressID
	 * @param account
	 * @return
	 */
	private ReturnMessage confirmOrder(Product product, Address address, Account account) throws Exception{
		//填充订单信息
		Order order = setOrderInfo(product, account);
		//填充订单配送信息
		Ordership ordership = setOrdershipInfo(address, order);
		order.setOrdership(ordership);
		//创建秒杀订单并插入到数据库
		Order orderData = null;
        try {
	        //orderData = orderService.createMiaoshaOrder(order);
	        mqProducer.sendMessage(order);
        } catch (Exception e) {
	        logger.error("创建订单失败", e);
	        RedisUtil.incr(RedisKeyPrefix.PRODUCT_STOCK + "_" + product.getId());
	        productSoldOutMap.remove(product.getId());
	        //修改zk的商品售完标记为false
	        if (zooKeeper.exists(ZookeeperPathPrefix.getZKSoldOutProductPath(product.getId()), true) != null) {
				zooKeeper.setData(ZookeeperPathPrefix.getZKSoldOutProductPath(product.getId()), "false".getBytes(), -1);
			}
	        return ReturnMessage.error("创建订单失败：" + e.getMessage());
        }
		
        //return ReturnMessage.success(orderData);
        //返回0代表排队中
        return ReturnMessage.success(0);
	}
	
    /**
     * @param productId
     * @return 秒杀成功返回"orderId"，秒杀失败返回"-1"，秒杀排队进行中返回"0"
     */
    @RequestMapping(value="result", method=RequestMethod.GET)
    @ResponseBody
    public ReturnMessage miaoshaResult(String productId) {
    	Account account = getLoginAccount();
		if(account==null){
			return ReturnMessage.error("必须登录才能参与秒杀");
		}
		
		//判断redis里的排队标记，排队标记不为空返回还在排队中(注意：一定要先判断排队标记再判断是否已生成订单，不然又会存在并发的时间差问题)
		if (RedisUtil.get(CommonMethod.getMiaoshaOrderWaitFlagRedisKey(account.getAccount(), productId)) != null) {
			//返回0代表排队中
			return ReturnMessage.success(0);
        }
		
		//TODO 查询用户秒杀商品订单是否创建成功
		Order order = RedisUtil.get(CommonMethod.getMiaoshaOrderRedisKey(account.getAccount(), productId), Order.class);
		if(order != null) {//秒杀成功
			return ReturnMessage.success(order);
		}
		
		//返回-1代表秒杀失败
		return ReturnMessage.error(-1, "秒杀失败");
    }
    
    /**
     * 获取秒杀接口的令牌
     * @param productId
     * @param verifyCode
     * @return
     */
    @RequestMapping(value="token", method=RequestMethod.GET)
    @ResponseBody
    public ReturnMessage getMiaoshaToken(HttpServletRequest request, String productId, String verifyCode) {
    	//用redis限流，限制接口1分钟最多访问10000次
    	Long requestNum = RedisUtil.incr(request.getRequestURI().toString());
		if (requestNum == 1) {
			RedisUtil.expire(request.getRequestURL().toString(), 60);
		} else if (requestNum > 100000) {
			return ReturnMessage.error("访问超载，请稍后再试");
		}
		
		
    	Account account = getLoginAccount();
		if(account==null){
			return ReturnMessage.error("必须登录才能参与秒杀");
		}
		//校验验证码，防止接口被刷
    	boolean check = checkVerifyCode(account, productId, verifyCode);
    	if(!check) {
    		return ReturnMessage.error("验证码错误");
    	}
    	String token = createMiaoshaToken(account, productId);
    	return ReturnMessage.success(token);
    }
    
    /**
     * 校验验证码
     * @param account
     * @param productId
     * @param verifyCode
     * @return
     */
    private boolean checkVerifyCode(Account account, String productId, String verifyCode) {
		if(account == null || StringUtils.isEmpty(verifyCode)) {
			return false;
		}
		String verifyCodeRedisKey = CommonMethod.getMiaoshaVerifyCodeRedisKey(account.getId(), productId);
		String realCode = RedisUtil.get(verifyCodeRedisKey);
		if(StringUtils.isEmpty(realCode) || !verifyCode.equals(realCode)) {
			return false;
		}
		RedisUtil.del(verifyCodeRedisKey);
		return true;
	}
    
    /**
     * 创建秒杀接口令牌
     * @param account
     * @param productId
     * @return
     */
    private String createMiaoshaToken(Account account, String productId) {
		if(account == null) {
			return null;
		}
		String token = MD5.md5(UUID.randomUUID().toString());
    	RedisUtil.set(CommonMethod.getMiaoshaTokenRedisKey(account.getId(), productId), token, 60);
		return token;
	}
    
    /**
     * 验证令牌的有效性
     * @param account
     * @param productId
     * @param token
     * @return
     */
    private boolean checkToken(Account account, String productId, String token) {
		if(account == null || token == null) {
			return false;
		}
		String realToken = RedisUtil.get(CommonMethod.getMiaoshaTokenRedisKey(account.getId(), productId), String.class);
		boolean result = token.equals(realToken);
		//验证完token需要立即销毁
		RedisUtil.del(CommonMethod.getMiaoshaTokenRedisKey(account.getId(), productId));
		return result;
	}
    
    
    /**
     * 获取验证码图片
     * @param response
     * @param productId
     * @return
     */
    @RequestMapping(value="verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public ReturnMessage getMiaoshaVerifyCod(HttpServletResponse response, String productId) {
    	Account account = getLoginAccount();
		if(account==null){
			return ReturnMessage.error("必须登录才能参与秒杀");
		}
    	try {
    		BufferedImage image  = createVerifyCode(account, productId);
    		OutputStream out = response.getOutputStream();
    		ImageIO.write(image, "JPEG", out);
    		out.flush();
    		out.close();
    		return null;
    	}catch(Exception e) {
    		e.printStackTrace();
    		return ReturnMessage.error("秒杀失败");
    	}
    }
    
	/**
	 * 校验商品的秒杀时间
	 * @param product
	 * @return
	 * @throws ParseException
	 */
	private ReturnMessage validMiaoshaTime(Product product) throws ParseException {
	    Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(product.getMiaoshaStartTime()));
		Calendar calendarEnd = Calendar.getInstance();
		calendarEnd.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(product.getMiaoshaEndTime()));
		long startAt = calendarStart.getTimeInMillis();
    	long endAt = calendarEnd.getTimeInMillis();
    	long now = System.currentTimeMillis();
    	if(now < startAt ) {//秒杀还没开始
    		return ReturnMessage.error("秒杀还没开始");
    	}else  if(now > endAt){//秒杀已经结束
    		return ReturnMessage.error("秒杀已经结束");
    	}
    	return ReturnMessage.success();
    }

	/**
	 * 填充订单配送信息
	 * @param selectAddressID
	 * @param order
	 * @return
	 */
	private Ordership setOrdershipInfo(Address address, Order order) {
		Ordership ordership = new Ordership();
		ordership.setOrderid(order.getId());
		
		Area area = SystemManager.getInstance().getAreaMap().get(address.getProvince());//获取省份对象
		String proinceName = area.getName();//省份名称
		String cityName = null;//城市名称
		String areaName = null;//区名称
		List<Area> citys = area.getChildren();
		if(citys!=null && citys.size()>0){
			for(int i=0;i<citys.size();i++){
				Area cityItem = citys.get(i);
				if(cityItem.getCode().equals(address.getCity())){
					cityName = cityItem.getName();
					//获取所在区域名称
					if(StringUtils.isNotBlank(address.getArea())){
						List<Area> areaList = cityItem.getChildren();
						if(areaList!=null && areaList.size()>0){
							for(int m=0;m<areaList.size();m++){
									areaName = areaList.get(m).getName();
							}
						}
					}
				}
			}
		}
		ordership.setShipname(address.getName());
		ordership.setShipaddress(proinceName+cityName+address.getAddress());
		ordership.setProvinceCode(address.getProvince());
		ordership.setProvince(proinceName);
		ordership.setCityCode(address.getCity());
		ordership.setCity(cityName);
		ordership.setAreaCode(address.getArea());
		ordership.setArea(areaName);
		ordership.setPhone(address.getPhone());
		ordership.setTel(address.getMobile());
		ordership.setZip(address.getZip());
		ordership.setSex("1");
		logger.info(ordership.toString());
		return ordership;
	}

	/**
	 * 填充订单基本信息
	 * @param product
	 * @param account
	 * @return
	 */
	private Order setOrderInfo(Product product, Account account) {
		//创建订单对象
		Order order = new Order();
		order.setAccount(account.getAccount());
		order.setQuantity(1);
		order.setRebate(1);
		order.setStatus(Order.order_status_init);
		order.setPaystatus(Order.order_paystatus_n);
		
		int score = 0;//订单积分 等于订单项中每个商品赠送的积分总和
		//创建订单明细集合
		List<Orderdetail> orderdetailList = new LinkedList<Orderdetail>();
		Orderdetail orderdetail = new Orderdetail();
		orderdetail.setProductID(Integer.valueOf(product.getId()));
		orderdetail.setGiftID(product.getGiftID());//商品赠品ID
		orderdetail.setPrice(product.getNowPrice());//商品现价
		orderdetail.setNumber(1);//购买数
		orderdetail.setFee("0");//配送费
		orderdetail.setProductName(product.getName());
		orderdetail.setTotal0(String.valueOf(Double.valueOf(orderdetail.getPrice()) * orderdetail.getNumber()));//订单项小计
		orderdetail.setScore(product.getScore());//活的赠送的积分
		orderdetailList.add(orderdetail);
		if(orderdetailList.size()==1){
			order.setRemark(orderdetailList.get(0).getProductName());
		}else{
			order.setRemark("合并|"+orderdetailList.size()+"笔订单");
		}
		order.setScore(score);
		order.setExpressCode("EXPRESS");//配送方式编码
		order.setExpressName("快递");//配送方式名称
		order.setFee("0");//订单配送费
		order.setPtotal(product.getNowPrice());//订单商品总金额
		order.setAmount(String.valueOf(Double.valueOf(order.getPtotal())+Double.valueOf(order.getFee())));//订单总金额 = 内存订单总金额 + 总配送费
		
		/**
		 * 对金额进行格式化，防止出现double型数字计算造成的益出。
		 */
		DecimalFormat df = new DecimalFormat("0.00");
		logger.info("order.getAmount()=" + order.getAmount());
		order.setAmount(df.format(Double.valueOf(order.getAmount())));//订单总金额
		order.setPtotal(df.format(Double.valueOf(order.getPtotal())));//订单商品总金额
		order.setFee(df.format(Double.valueOf(order.getFee())));//订单总配送费
		
		order.setOrderdetail(orderdetailList);
		return order;
	}
	
	
	/**
	 * 创建验证码
	 * @param account
	 * @param productId
	 * @return
	 */
	private BufferedImage createVerifyCode(Account account, String productId) {
		if(account == null) {
			return null;
		}
		int width = 80;
		int height = 32;
		//create the image
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		// set the background color
		g.setColor(new Color(0xDCDCDC));
		g.fillRect(0, 0, width, height);
		// draw the border
		g.setColor(Color.black);
		g.drawRect(0, 0, width - 1, height - 1);
		// create a random instance to generate the codes
		Random rdm = new Random();
		// make some confusion
		for (int i = 0; i < 50; i++) {
			int x = rdm.nextInt(width);
			int y = rdm.nextInt(height);
			g.drawOval(x, y, 0, 0);
		}
		// generate a random code
		String verifyCode = generateVerifyCode(rdm);
		g.setColor(new Color(0, 100, 0));
		g.setFont(new Font("Candara", Font.BOLD, 24));
		g.drawString(verifyCode, 8, 24);
		g.dispose();
		//把验证码存到redis中
		Integer result = calc(verifyCode);  //12
		if (result == null) {
	        return null;
        }
		String verifyCodeRedisKey = CommonMethod.getMiaoshaVerifyCodeRedisKey(account.getId(), productId);
		RedisUtil.set(verifyCodeRedisKey, result.toString(), 300);
		//输出图片	
		return image;
	}
	
	private static Integer calc(String exp) {
		try {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			return (Integer)engine.eval(exp);
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static char[] ops = new char[] {'+', '-', '*'};
	/**
	 * + - * 
	 * */
	private String generateVerifyCode(Random rdm) {
		int num1 = rdm.nextInt(10);
	    int num2 = rdm.nextInt(10);
		int num3 = rdm.nextInt(10);
		char op1 = ops[rdm.nextInt(3)];
		char op2 = ops[rdm.nextInt(3)];
		String exp = ""+ num1 + op1 + num2 + op2 + num3;
		return exp;
	}
	
}