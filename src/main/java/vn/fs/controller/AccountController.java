package vn.fs.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import vn.fs.model.dto.ChangePassword;
import vn.fs.entities.UserEntity;
import vn.fs.repository.UserRepository;
import vn.fs.service.ISendMailService;

/**
 * @author DongTHD
 *
 */
@Controller
public class AccountController {

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	HttpSession session;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ISendMailService sendMailService;

	@GetMapping(value = "/forgotPassword")
	public String forgotPassword() {
		// return "web/forgotPassword";
		return "web/accuracy";
	}

	@PostMapping("/forgotPasswordGmail")
	public ModelAndView forgotPassowrd(ModelMap model, @RequestParam("email") String email) {
		List<UserEntity> listUser = userRepository.findAll();
		for (UserEntity user : listUser) {
			if (email.trim().equals(user.getEmail())) {
				session.removeAttribute("otp");
				int random_otp = (int) Math.floor(Math.random() * (999999 - 100000 + 1) + 100000);
				session.setAttribute("otp", random_otp);
				String body = "<div>\r\n"
						+ "<h3>Mã xác thực OTP của bạn là: <span style=\"color:#119744; font-weight: bold;\">"
						+ random_otp + "</span></h3>\r\n" + "</div>";
				sendMailService.queue(email, "Quên mật khẩu?", body);

				model.addAttribute("email", email);
				model.addAttribute("message", "Mã xác thực OTP đã được gửi tới Email : " + user.getEmail()
						+ " , hãy kiểm tra Email của bạn!");
				return new ModelAndView("/web/confirmOtpForgotPassword", model);
			}
		}
		model.addAttribute("error", "Email này chưa đăng ký!");
		return new ModelAndView("web/forgotPasswordGmail", model);
	}

	@PostMapping("/confirmOtpForgotPassword")
	public ModelAndView confirm(ModelMap model, @RequestParam("otp") String otp, @RequestParam("email") String email) {
		if (otp.equals(String.valueOf(session.getAttribute("otp")))) {
			model.addAttribute("email", email);
			model.addAttribute("newPassword", "");
			model.addAttribute("confirmPassword", "");
			model.addAttribute("changePassword", new ChangePassword());
			return new ModelAndView("web/changePassword", model);
		}
		model.addAttribute("error", "Mã xác thực OTP không đúng, thử lại!");
		return new ModelAndView("web/confirmOtpForgotPassword", model);
	}

	@PostMapping("/changePassword")
	public ModelAndView changeForm(ModelMap model,
			@Valid @ModelAttribute("changePassword") ChangePassword changePassword, BindingResult result,
			@RequestParam("email") String email, @RequestParam("newPassword") String newPassword,
			@RequestParam("confirmPassword") String confirmPassword) {
		if (result.hasErrors()) {

			model.addAttribute("newPassword", newPassword);
			model.addAttribute("newPassword", confirmPassword);
			model.addAttribute("email", email);
			return new ModelAndView("/web/changePassword", model);
		}

		if (!changePassword.getNewPassword().equals(changePassword.getConfirmPassword())) {

			model.addAttribute("newPassword", newPassword);
			model.addAttribute("newPassword", confirmPassword);
			model.addAttribute("error", "error");
			model.addAttribute("email", email);
			return new ModelAndView("/web/changePassword", model);
		}
		UserEntity user = userRepository.findByEmail(email);
		user.setStatus(true);
		user.setPassword(bCryptPasswordEncoder.encode(newPassword));
		userRepository.save(user);
		model.addAttribute("message", "Đặt lại mật khẩu thành công!");
		model.addAttribute("email", "");
		session.removeAttribute("otp");
		return new ModelAndView("/web/changePassword", model);
	}

	@PostMapping("/authenticationMethod")
	public ModelAndView accuracyOTP(ModelMap model, HttpServletRequest request) {
		String accuracy = request.getParameter("accuracy");
		if (accuracy.equals("gmail")) {
			return new ModelAndView("web/forgotPasswordGmail", model);
		}
		if (accuracy.equals("sms")) {
			return new ModelAndView("web/forgotPasswordSMS", model);
		}
		if (accuracy.equals("voicecall")) {
			return new ModelAndView("web/forgotPasswordVoiceCall", model);
		}
		return new ModelAndView("web/accuracy", model);
	}
	
	@PostMapping("/forgotPasswordSMS")
	public ModelAndView forgotPasswordSMS (ModelMap modelMap,@RequestParam("sms") String sms) {
		return new ModelAndView("web/forgotPasswordSMS", modelMap);
	}
	@PostMapping("/forgotPasswordVoiceCall")
	public ModelAndView forgotPasswordVoiceCall (ModelMap modelMap,@RequestParam("voicecall") String sms) {
		return new ModelAndView("web/forgotPasswordVoiceCall", modelMap);
	}
}
