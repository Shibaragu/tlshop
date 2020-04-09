package com.jiagouedu.web.action.manage.pay;import com.jiagouedu.core.KeyValueHelper;import com.jiagouedu.services.manage.pay.PayService;import com.jiagouedu.services.manage.pay.bean.Pay;import com.jiagouedu.web.action.BaseController;import org.apache.commons.lang.StringUtils;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Controller;import org.springframework.web.bind.annotation.ModelAttribute;import org.springframework.web.bind.annotation.RequestMapping;import org.springframework.web.servlet.mvc.support.RedirectAttributes;import javax.servlet.http.HttpServletRequest;/** * 支付方式 * @author wukong 图灵学院 QQ:245553999 * @author wukong 图灵学院 QQ:245553999 * */@Controller@RequestMapping("/manage/pay/")public class PayAction extends BaseController<Pay> {	private static final long serialVersionUID = 1L;	private static final Logger logger = LoggerFactory.getLogger(PayAction.class);	@Autowired	private PayService payService;	private static final String page_toList = "/manage/pay/payList";	private static final String page_toEdit = "/manage/pay/payEdit";	private static final String page_toAdd = "/manage/pay/payEdit";	private PayAction() {		super.page_toList = page_toList;		super.page_toAdd = page_toAdd;		super.page_toEdit = page_toEdit;	}	public PayService getService() {		return payService;	}	@Override	public String insert(HttpServletRequest request, Pay e, RedirectAttributes flushAttrs) throws Exception {		throw new RuntimeException("非法请求！");	}		//根据code获取名称	private void comm(Pay e) {		logger.error("comm..code="+e.getCode());		String name = KeyValueHelper.get("pay_code_"+e.getCode());		if(StringUtils.isBlank(name)){			throw new NullPointerException("未配置"+e.getCode()+"的支付方式的键值对！");		}				e.setName(name);	}		@Override	public String update(HttpServletRequest request, Pay e, RedirectAttributes flushAttrs) throws Exception {		comm(e);		return super.update(request, e, flushAttrs);	}		@Override	public String deletes(HttpServletRequest request, String[] ids, @ModelAttribute("e") Pay e, RedirectAttributes flushAttrs) throws Exception {		throw new RuntimeException("非法请求！");	}}