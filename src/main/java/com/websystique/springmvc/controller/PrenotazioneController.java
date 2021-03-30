package com.websystique.springmvc.controller;

import com.websystique.springmvc.dto.PrenotazioneDto;
import com.websystique.springmvc.model.Auto;
import com.websystique.springmvc.model.Prenotazione;
import com.websystique.springmvc.service.AutoService;
import com.websystique.springmvc.service.PrenotazioneService;
import com.websystique.springmvc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@Controller
@RequestMapping("/")
@SessionAttributes("roles")
public class PrenotazioneController {

	@Autowired
	PrenotazioneService prenotazioneService;

	@Autowired
	UserService userService;

	@Autowired
	AutoService autoService;

	@Autowired
	MessageSource messageSource;

	@Autowired
	PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices;

	@Autowired
	AuthenticationTrustResolver authenticationTrustResolver;


	/**
	 * This method will list all existing users.
	 */
	@RequestMapping(value = {"/prenotazionelist"}, method = RequestMethod.GET)
	public String listPrenotazioni(ModelMap model) {
		List<PrenotazioneDto> prenotaziones = prenotazioneService.findAllPrenotaziones();
		model.addAttribute("prenotaziones", prenotaziones);
		model.addAttribute("loggedinuser", getPrincipal());
		return "prenotazionelist";
	}

	/*@RequestMapping(value = {"/prenotazioni-user-0"}, method = RequestMethod.GET)
	public String listPrenotazionibyUser0(@PathVariable int id,ModelMap model) {

		return "redirect:/prenotazioni-user-" + userService.findByUsername(getPrincipal()).getId();
	}*/

	@PreAuthorize("authentication.principal.username == #username) || hasRole('ADMIN')")
	@RequestMapping(value = {"/prenotazioni-user-{id}"}, method = RequestMethod.GET)
	public String listPrenotazionibyUser(@PathVariable int id,ModelMap model) {
		if(id==0){
			id=userService.findByUsername(getPrincipal()).getId();
		}
		List<PrenotazioneDto> prenotaziones = prenotazioneService.findByUser(id);
		model.addAttribute("prenotaziones", prenotaziones);
		model.addAttribute("loggedinuser", getPrincipal());
		return "prenotazionelist";
	}

	/**
	 * This method will provide the medium to add a new user.
	 */
	@RequestMapping(value = {"/newprenotazione"}, method = RequestMethod.GET)
	public String newPrenotazione(ModelMap model) {
		PrenotazioneDto prenotazione = new PrenotazioneDto();
		model.addAttribute("prenotazioneDto", prenotazione);
		model.addAttribute("edit", false);
		model.addAttribute("loggedinuser", getPrincipal());
		return "prenotazioneregistration";
	}

	/**
	 * This method will be called on form submission, handling POST request for
	 * saving user in database. It also validates the user input
	 */
	@RequestMapping(value = {"/newprenotazione"}, method = RequestMethod.POST)
	public String savePrenotazione(@ModelAttribute("prenotazioneDto")@Valid PrenotazioneDto prenotazioneDto, BindingResult result,
								   ModelMap model) {


		if (result.hasErrors()) {
			model.addAttribute("loggedinuser", getPrincipal());
			return "prenotazioneregistration";
		}

		/*
		 * Preferred way to achieve uniqueness of field [sso] should be implementing custom @Unique annotation
		 * and applying it on field [sso] of Model class [User].
		 *
		 * Below mentioned peace of code [if block] is to demonstrate that you can fill custom errors outside the validation
		 * framework as well while still using internationalized messages.
		 *
		 */
		/*result=controlDates(prenotazioneDto,result);
		if (result.hasErrors()) {
			return "prenotazioneregistration";
		}*/
		if( userService.findByUsername(prenotazioneDto.getUsername())==null){
			FieldError autouser =new FieldError("prenotazioneDto","username",messageSource.getMessage("non.existing.username", new String[]{prenotazioneDto.getTarga()}, Locale.getDefault()));
			result.addError(autouser);
			model.addAttribute("loggedinuser", getPrincipal());
			return "prenotazioneregistration";
		}
		if(autoService.findByTarga(prenotazioneDto.getTarga())==null){
			FieldError autouser =new FieldError("prenotazioneDto","targa",messageSource.getMessage("non.existing.targa", new String[]{prenotazioneDto.getTarga()}, Locale.getDefault()));
			result.addError(autouser);
			model.addAttribute("loggedinuser", getPrincipal());
			return "prenotazioneregistration";
		}
		if(prenotazioneDto.getDataDiFine().before(prenotazioneDto.getDataDiFine())){
			FieldError inversiondate =new FieldError("prenotazioneDto","dataDiFine",messageSource.getMessage("prenotazione.inversiondate", null, Locale.getDefault()));
			result.addError(inversiondate);
			model.addAttribute("loggedinuser", getPrincipal());
			return "prenotazioneregistration";
		}
		List<PrenotazioneDto> prenotazioneDtos=prenotazioneService.findByAuto(autoService.findByTarga(prenotazioneDto.getTarga()));
		for (PrenotazioneDto var :
				prenotazioneDtos) {
			if(		var.getDataDiFine().equals(prenotazioneDto.getDataDiFine()) || var.getDataDiInizio().equals(prenotazioneDto.getDataDiInizio()) ||
					var.getDataDiInizio().equals(prenotazioneDto.getDataDiFine()) || var.getDataDiFine().equals(prenotazioneDto.getDataDiInizio()) ||
					(prenotazioneDto.getDataDiFine().before(var.getDataDiFine()) && prenotazioneDto.getDataDiFine().after(var.getDataDiInizio())) ||
					(prenotazioneDto.getDataDiInizio().before(var.getDataDiFine()) && prenotazioneDto.getDataDiInizio().after(var.getDataDiInizio()))){
				FieldError troppotardi =new FieldError("prenotazioneDto","dataDiFine",messageSource.getMessage("prenotazione.dateoccupate", null, Locale.getDefault()));
				result.addError(troppotardi);
				model.addAttribute("loggedinuser", getPrincipal());
				return "prenotazioneregistration";
			}
		}


		prenotazioneService.savePrenotazione(prenotazioneDto);

		model.addAttribute("success", "prenotazione " + prenotazioneDto.getId() + " registered successfully");
		model.addAttribute("loggedinuser", getPrincipal());
		model.addAttribute("returnpage", "prenotazione");

		return "registrationsuccess";
	}

	private BindingResult controlDates(PrenotazioneDto prenotazioneDto,BindingResult result) {
		if(autoService.findByTarga(prenotazioneDto.getTarga())==null || userService.findByUsername(prenotazioneDto.getUsername())==null){
			FieldError autouser =new FieldError("prenotazione","user",messageSource.getMessage("non.existing.autouser", null, Locale.getDefault()));
			result.addError(autouser);
		}
		if(prenotazioneDto.getDataDiFine().before(prenotazioneDto.getDataDiFine())){
			FieldError inversiondate =new FieldError("prenotazione","dataDiFine",messageSource.getMessage("prenotazione.inversiondate", null, Locale.getDefault()));
			result.addError(inversiondate);
		}
		List<PrenotazioneDto> prenotazioneDtos=prenotazioneService.findByAuto(autoService.findByTarga(prenotazioneDto.getTarga()));
		for (PrenotazioneDto var :
				prenotazioneDtos) {
			if(		var.getDataDiFine().equals(prenotazioneDto.getDataDiFine()) || var.getDataDiInizio().equals(prenotazioneDto.getDataDiInizio()) ||
					var.getDataDiInizio().equals(prenotazioneDto.getDataDiFine()) || var.getDataDiFine().equals(prenotazioneDto.getDataDiInizio()) ||
					(prenotazioneDto.getDataDiFine().before(var.getDataDiFine()) && prenotazioneDto.getDataDiFine().after(var.getDataDiInizio())) ||
					(prenotazioneDto.getDataDiInizio().before(var.getDataDiFine()) && prenotazioneDto.getDataDiInizio().after(var.getDataDiInizio()))){
				FieldError troppotardi =new FieldError("prenotazione","dataDiFine",messageSource.getMessage("prenotazione.dateoccupate", null, Locale.getDefault()));
				result.addError(troppotardi);
			}
		}
		return result;
	}


	/**
	 * This method will provide the medium to update an existing user.
	 */
	@RequestMapping(value = {"/edit-prenotazione-{id}"}, method = RequestMethod.GET)
	public String editPrenotazione(@PathVariable int id, ModelMap model) {
		PrenotazioneDto prenotazione = prenotazioneService.findById(id);
		model.addAttribute("prenotazione", prenotazione);
		model.addAttribute("edit", true);
		model.addAttribute("loggedinuser", getPrincipal());
		return "prenotazioneregistration";
	}

	/**
	 * This method will be called on form submission, handling POST request for
	 * updating user in database. It also validates the user input
	 */
	@PreAuthorize("authentication.principal.username == #username)")
	@RequestMapping(value = {"/edit-prenotazione-{id}"}, method = RequestMethod.POST)
	public String updatePrenotazione(@PathVariable int id,@Valid PrenotazioneDto prenotazioneDto, BindingResult result,
							 ModelMap model) {

		if (result.hasErrors()) {
			return "prenotazioneregistration";
		}

		/*//Uncomment below 'if block' if you WANT TO ALLOW UPDATING SSO_ID in UI which is a unique key to a User.
		if(!userService.isUserSSOUnique(user.getId(), user.getSsoId())){
			FieldError ssoError =new FieldError("user","ssoId",messageSource.getMessage("non.unique.ssoId", new String[]{user.getSsoId()}, Locale.getDefault()));
		    result.addError(ssoError);
			return "registration";
		}*/

		result=controlDates(prenotazioneDto,result);
		if (result.hasErrors()) {
			return "prenotazioneregistration";
		}
		prenotazioneDto.setApprovata(false);
		prenotazioneService.updatePrenotazione(prenotazioneDto);

		model.addAttribute("success", "prenotazione " + prenotazioneDto.getId() + " updated successfully");
		model.addAttribute("loggedinuser", getPrincipal());
		model.addAttribute("returnpage", "prenotazione");
		return "registrationsuccess";
	}


	/**
	 * This method will delete an user by it's SSOID value.
	 */
	@RequestMapping(value = {"/delete-prenotazione-{id}"}, method = RequestMethod.GET)
	public String deletePrenotazione(@PathVariable int id) {
		prenotazioneService.deletePrenotazioneById(id);
		return "redirect:/prenotazionelist";
	}

	@RequestMapping(value = {"/approve-prenotazione-{id}"}, method = RequestMethod.GET)
	public String approvePrenotazione(@PathVariable int id) {
		PrenotazioneDto prenotazione=prenotazioneService.findById(id);
		prenotazione.setApprovata(true);
		prenotazioneService.updatePrenotazione(prenotazione);
		return "redirect:/prenotazionelist";
	}

	@RequestMapping(value = {"/disapprove-prenotazione-{id}"}, method = RequestMethod.GET)
	public String disapprovePrenotazione(@PathVariable int id) {
		PrenotazioneDto prenotazione=prenotazioneService.findById(id);
		prenotazione.setApprovata(false);
		prenotazioneService.updatePrenotazione(prenotazione);
		return "redirect:/prenotazionelist";
	}


	/**
	 * This method will provide UserProfile list to views
	 */
	/*@ModelAttribute("roles")
	public List<UserProfile> initializeProfiles() {
		return userProfileService.findAll();
	}*/

	/**
	 * This method handles Access-Denied redirect.
	 */
	/*@RequestMapping(value = "/Access_Denied", method = RequestMethod.GET)
	public String accessDeniedPage(ModelMap model) {
		model.addAttribute("loggedinuser", getPrincipal());
		return "accessDenied";
	}*/


	/**
	 * This method returns the principal[user-name] of logged-in user.
	 */
	private String getPrincipal() {
		String userName;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (principal instanceof UserDetails) {
			userName = ((UserDetails) principal).getUsername();
		} else {
			userName = principal.toString();
		}
		return userName;
	}

	/**
	 * This method returns true if users is already authenticated [logged-in], else false.
	 */
	private boolean isCurrentAuthenticationAnonymous() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authenticationTrustResolver.isAnonymous(authentication);
	}

}