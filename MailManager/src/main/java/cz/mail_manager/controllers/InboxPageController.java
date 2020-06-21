package cz.mail_manager.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import cz.mail_manager.pojo.Email;
import cz.mail_manager.server_connection.ReceivedEmails;

@Controller
@SessionAttributes("email")
public class InboxPageController {
	
	@Autowired
	private ReceivedEmails receivedEmails;
	
	/**
	 * 	Doručené emaily
	 * 
	 * 	@param model
	 * 
	 * 	@return - vrací přehled doručených emailů
	 */
	@RequestMapping(value = "/dorucene", method = RequestMethod.GET)
	public String submit(Model model) {

		List<Email> emails = receivedEmails.getEmailsHeader("INBOX", 0);
		model.addAttribute("emails", emails);
		
		model.addAttribute("messageCount", receivedEmails.getMessageCount());
		
		return "emailViewPage";
	}
	
	/**
	 * 	Ajax zobrazení detailu emailů
	 * 
	 *  @param model
	 * 	@param detailIndex - index vybraného emailu
	 *  
	 * 	@return vrací kompletní informace o emailu
	 */
	@RequestMapping(value = "/dorucene/detail", method = RequestMethod.POST)
	public @ResponseBody Email getEmailContent (Model model, int detailIndex) {
		
		Email email = receivedEmails.getEmailDetail("INBOX", detailIndex);
		
		// Přidání emailu do modelu, pokud obsahuje přiložené soubory
		if (email.getAttachedFiles() != null) {
			
			model.addAttribute("email", email);
		}
		
		return email;
	}
	
	/**
	 * 	Zobrazení přiloženého souboru
	 * 
	 * 	@param index - index přílohy
	 * 	@param email - vybraný email
	 * 	@param response - response
	 * 	@param model
	 */
	@RequestMapping(value = "/dorucene/priloha${index}/*", method = RequestMethod.GET)
	public @ResponseBody void getAttachedFile(@PathVariable("index") String index, 
											  @ModelAttribute("email") Email email, 
											  HttpServletResponse response,
											  Model model) throws IOException {

		InputStream inputStream = email.getAttachedFiles().get(Integer.parseInt(index)).getFileInputStream();
		IOUtils.copy(inputStream, response.getOutputStream());
	}
	
	/**
	 * 	Ajax přesunutí emailu do jiné složky
	 * 
	 * 	@param detailIndex - index vybraného emailu
	 * 	@param folder - nová složka
	 */
	@RequestMapping(value = "/dorucene/presun", method = RequestMethod.POST)
	public @ResponseBody void moveEmail(int detailIndex, String folder) {
		
		receivedEmails.move("INBOX", detailIndex, folder);
	}
	
	/**
	 * 	Ajax smazání emailu	
	 * 
	 * 	@param detailIndex - index vybraného emailu
	 */
	@RequestMapping(value = "/dorucene/smazat", method = RequestMethod.POST)
	public @ResponseBody void deleteEmail(int detailIndex) {
		
		receivedEmails.delete("INBOX", detailIndex);
	}
	
	/**
	 * 	Ajax načtení dalších emailů
	 * 
	 * 	@param lastIndex - index posledního emailu
	 * 
	 * 	@return vrací List dalších emailů
	 */
	@RequestMapping(value = "/dorucene/nacteni", method = RequestMethod.POST)
	public @ResponseBody List<Email> getMoreEmails(int lastIndex) {
		
		List<Email> emails = receivedEmails.getEmailsHeader("INBOX", lastIndex);
		
		return emails;
	}
	
	/**
	 * 	Ajax obnovení nových emailů
	 * 	
	 * 	@param sentDate - datum odeslání emailu
	 * 
	 * 	@return vrací List nových emailů
	 */
	@RequestMapping(value = "/dorucene/obnoveni", method = RequestMethod.POST)
	public @ResponseBody List<Email> refresh(String sentDate) {
		
		List<Email> emails = receivedEmails.refreshEmailsHeader("INBOX", sentDate);
		
		return emails;
	}
	
}
