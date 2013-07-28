/**
 * Most of the code in the Qalingo project is copyrighted Hoteia and licensed
 * under the Apache License Version 2.0 (release version 0.7.0)
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *                   Copyright (c) Hoteia, 2012-2013
 * http://www.hoteia.com - http://twitter.com/hoteia - contact@hoteia.com
 *
 */
package fr.hoteia.qalingo.web.mvc.controller.customer;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import fr.hoteia.qalingo.core.ModelConstants;
import fr.hoteia.qalingo.core.domain.Customer;
import fr.hoteia.qalingo.core.domain.Localization;
import fr.hoteia.qalingo.core.domain.Market;
import fr.hoteia.qalingo.core.domain.MarketArea;
import fr.hoteia.qalingo.core.domain.MarketPlace;
import fr.hoteia.qalingo.core.domain.Retailer;
import fr.hoteia.qalingo.core.web.servlet.ModelAndViewThemeDevice;
import fr.hoteia.qalingo.web.mvc.form.CustomerEditForm;
import fr.hoteia.qalingo.web.mvc.viewbean.CustomerViewBean;
import fr.hoteia.qalingo.web.service.WebCommerceService;

/**
 * 
 */
@Controller("customerDetailsController")
public class CustomerDetailsController extends AbstractCustomerController {

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
    protected WebCommerceService webCommerceService;
	
	@RequestMapping(value = "/customer-details.html*", method = RequestMethod.GET)
	public ModelAndView customerDetails(final HttpServletRequest request, final Model model) throws Exception {
		ModelAndViewThemeDevice modelAndView = new ModelAndViewThemeDevice(getCurrentVelocityPath(request), "customer/customer-details");
		
		// "customer.details";
		final Customer customer = requestUtil.getCurrentCustomer(request);

		final MarketPlace currentMarketPlace = requestUtil.getCurrentMarketPlace(request);
		final Market currentMarket = requestUtil.getCurrentMarket(request);
		final MarketArea currentMarketArea = requestUtil.getCurrentMarketArea(request);
		final Localization currentLocalization = requestUtil.getCurrentLocalization(request);
		final Retailer currentRetailer = requestUtil.getCurrentRetailer(request);
		final CustomerViewBean customerViewBean = viewBeanFactory.buildCustomerDetailsAccountViewBean(request, currentMarketPlace, currentMarket, currentMarketArea, currentLocalization, currentRetailer, customer);
		modelAndView.addObject("customerDetails", customerViewBean);

        return modelAndView;
	}
	
	@RequestMapping(value = "/customer-edit.html*", method = RequestMethod.GET)
	public ModelAndView displayCustomerEdit(final HttpServletRequest request, final Model model, @ModelAttribute("customerEditForm") CustomerEditForm customerEditForm) throws Exception {
		ModelAndViewThemeDevice modelAndView = new ModelAndViewThemeDevice(getCurrentVelocityPath(request), "customer/customer-edit-form");
		
		// "customer.details";
		final Customer customer = requestUtil.getCurrentCustomer(request);

		if(customerEditForm == null 
        		|| customerEditForm.equals(new CustomerEditForm())){
			customerEditForm = formFactory.buildCustomerEditForm(request, customer);
			model.addAttribute("customerEditForm", customerEditForm);
		}
		
        return modelAndView;
	}
	
	@RequestMapping(value = "/customer-edit.html*", method = RequestMethod.POST)
	public ModelAndView submitCustomerEdit(final HttpServletRequest request, @Valid @ModelAttribute("customerEditForm") CustomerEditForm customerEditForm,
								BindingResult result, final Model model) throws Exception {
		final MarketPlace currentMarketPlace = requestUtil.getCurrentMarketPlace(request);
		final Market currentMarket = requestUtil.getCurrentMarket(request);
		final MarketArea currentMarketArea = requestUtil.getCurrentMarketArea(request);
		final Localization currentLocalization = requestUtil.getCurrentLocalization(request);
		final Retailer currentRetailer = requestUtil.getCurrentRetailer(request);
		
		// "customer.details";

		if (result.hasErrors()) {
			return displayCustomerEdit(request, model, customerEditForm);
		}
		
		final String newEmail = customerEditForm.getEmail();
		final Customer currentCustomer = requestUtil.getCurrentCustomer(request);
		final Customer checkCustomer = customerService.getCustomerByLoginOrEmail(newEmail);
		if(checkCustomer != null
				&& !currentCustomer.getEmail().equalsIgnoreCase(newEmail)) {
			final String forgottenPasswordUrl = urlService.buildForgottenPasswordUrl(request, currentMarketPlace, currentMarket, currentMarketArea, currentLocalization, currentRetailer);
			final Object[] objects = {forgottenPasswordUrl};
			result.rejectValue("email", "fo.customer.error_form_create_account_account_already_exist", objects,"This email customer account already exist! Go on this <a href=\"${0}\" alt=\"\">page</a> to get a new password.");
		}

		// Update the customer
		webCommerceService.updateCurrentCustomer(request, currentMarket, currentMarketArea, customerEditForm);
		
		final String urlRedirect = urlService.buildCustomerDetailsUrl(request, currentMarketArea);
        return new ModelAndView(new RedirectView(urlRedirect));
	}
	
    @ModelAttribute
    public void commonValues(HttpServletRequest request, Model model) throws Exception {
		final MarketArea currentMarketArea = requestUtil.getCurrentMarketArea(request);
    	model.addAttribute(ModelConstants.URL_BACK, urlService.buildCustomerDetailsUrl(request, currentMarketArea));
    	model.addAttribute(ModelConstants.URL_CUSTOMER_EDIT, urlService.buildCustomerEditUrl(request, currentMarketArea));
    }
    
}