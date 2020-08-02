package com.jiagouedu.services.front.product.impl;import com.jiagouedu.core.ServersManager;import com.jiagouedu.services.front.product.ProductService;import com.jiagouedu.services.front.product.bean.Product;import com.jiagouedu.services.front.product.bean.ProductStockInfo;import com.jiagouedu.services.front.product.dao.ProductDao;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Service;import javax.annotation.Resource;import java.util.List;@Service("productServiceImpl")public class ProductServiceImpl extends ServersManager<Product, ProductDao> implements		ProductService {    @Override	@Resource(name = "productDaoImpl")    public void setDao(ProductDao productDao) {        this.dao = productDao;    }	@Override	public void upGoods(String[] ids) {		upOrDown(ids, 1);	}	@Override	public void downGoods(String[] ids) {		upOrDown(ids, 2);	}	/**	 * @param ids	 */	private void upOrDown(String[] ids, int status) {		if (ids == null || ids.length == 0) {			return;		}		for (int i = 0; i < ids.length; i++) {			Product goods = new Product();			goods.setId(ids[i]);			goods.setStatus(status);			dao.update(goods);		}	}	@Override	public List<Product> search(Product e) {		return dao.selectList(e);	}	@Override	public void updateStockAfterPaySuccess(Product product) {		dao.updateStockAfterPaySuccess(product);	}		@Override	public int updateStockAfterMiaoshaSuccess(Product product) {		//TODO 插入log(orderId,productId,deductStockCount,datetime)		return dao.updateStockAfterMiaoshaSuccess(product);	}	@Override	public List<Product> selectParameterList(String id) {		return dao.selectParameterList(id);	}	@Override	public List<ProductStockInfo> selectStockList(Product product) {		return dao.selectStockList(product);	}	@Override	public List<Product> selectListProductHTML(Product product) {		return dao.selectListProductHTML(product);	}	@Override	public List<Product> selectProductListByIds(Product p) {		return dao.selectProductListByIds(p);	}	@Override	public List<Product> selectHotSearch(Product p) {		return dao.selectHotSearch(p);	}	@Override	public List<Product> loadHotProductShowInSuperMenu(Product product) {		return dao.loadHotProductShowInSuperMenu(product);	}	@Override	public void updateHit(Product p) {        dao.updateHit(p);	}	@Override	public List<Product> selectPageLeftHotProducts(Product p) {		return dao.selectPageLeftHotProducts(p);	}	@Override	public List<Product> selectActivityProductList(Product p) {		return dao.selectActivityProductList(p);	}}