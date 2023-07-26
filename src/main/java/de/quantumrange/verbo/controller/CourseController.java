package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.*;
import de.quantumrange.verbo.service.ControlService;
import de.quantumrange.verbo.service.repos.CourseRepository;
import de.quantumrange.verbo.service.repos.InviteRepository;
import de.quantumrange.verbo.service.repos.UserRepository;
import de.quantumrange.verbo.service.repos.WordSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class CourseController {
	
	private final ControlService controlService;
	private final UserRepository userRepository;
	private final CourseRepository courseRepository;
	private final WordSetRepository wordSetRepository;
	
	@Autowired
	public CourseController(ControlService controlService,
	                        UserRepository userRepository,
	                        CourseRepository courseRepository,
	                        WordSetRepository wordSetRepository) {
		this.controlService = controlService;
		this.userRepository = userRepository;
		this.courseRepository = courseRepository;
		this.wordSetRepository = wordSetRepository;
	}
	
	@GetMapping("courses")
	@PreAuthorize("hasAnyAuthority('site:courses')")
	public String courses(Principal principal,
	                      Model model,
	                      @RequestParam(name = "error", defaultValue = "-1") int code,
	                      @RequestParam(name = "all", defaultValue = "") String showAll) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.COURSES)
				.orElseThrow();
		
		if (user.isForcedPasswordChange()) return "redirect:/myAccount";
		
		boolean all = showAll.equals("true") && user.hasPermission(Permission.COURSE_VIEW_ALL);
		
		model.addAttribute("all", all);
		
		List<Course> list = all ? courseRepository.findAll() : user.getCourses();
		
		model.addAttribute("list", list);
		if (code == 0) model.addAttribute("errorCreate", true);
		if (code == 1) model.addAttribute("errorJoin", true);
		
		return "courses";
	}
	
	@PostMapping("course/create")
	@PreAuthorize("hasAnyAuthority('course:create')")
	public void create(HttpServletResponse response,
	                   Principal principal,
	                   @RequestParam(name = "name") String name) throws IOException {
		User user = userRepository.findByPrinciple(principal)
				.orElseThrow();
		
		name = name.replaceAll("[^A-Za-z\\sáéíóúàèìòùÁÉÍÓÚÀÈÌÒÙ\\-_+=:;<.>,?!@0-9%$#]", "");
		name = name.replaceFirst(" ", "");
		name = name.substring(0, Math.min(name.length(), 32));
		
		if (name.isBlank() || courseRepository.existsByNameAllIgnoreCase(name)) {
			response.sendRedirect("/courses?error=0");
			return;
		}
		
		Course course = courseRepository.saveAndFlush(new Course(0L,
				name,
				user,
				new HashSet<>(),
				new HashSet<>(),
				"No upcoming tests :)",
				new HashSet<>(),
				Invite.generateCode()));
		
		course.getUsers().add(user);
		
		courseRepository.saveAndFlush(course);
		
		response.sendRedirect("/course/" + course.getVisibleId() + "/");
	}
	
	@PostMapping("course/join")
	@PreAuthorize("hasAnyAuthority('course:join')")
	public void join(HttpServletResponse response,
	                 Principal principal,
	                 @RequestParam(name = "code") String code) throws IOException {
		User user = userRepository.findByPrinciple(principal)
				.orElseThrow();
		
		Optional<Course> optional = courseRepository.findByCode(code);
		
		if (optional.isEmpty()) {
			response.sendRedirect("/courses?error=1");
			return;
		}
		
		Course course = optional.get();
		
		if (!course.getUsers().contains(user)) {
			course.getUsers().add(user);
			
			courseRepository.save(course);
		}
		
		response.sendRedirect("/course/" + course.getVisibleId() + "/");
	}
	
	@GetMapping("course/{id}/")
	@PreAuthorize("hasAnyAuthority('site:course')")
	public String course(Principal principal,
	                     Model model,
	                     @PathVariable(name = "id") String id) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.COURSES)
				.orElseThrow();
		if (user.isForcedPasswordChange()) return "redirect:/myAccount";
		
		Course course = courseRepository.findById(Identifiable.getId(id))
				.orElseThrow();
		
		if (!course.getUsers().contains(user)) return "redirect:/home";
		
		model.addAttribute("user", user);
		model.addAttribute("course", course);
		model.addAttribute("sets", course.getWordSets());
		model.addAttribute("currentSets", course.getWordSetInTest());
		
		return "course";
	}
	
	@GetMapping("course/{id}/edit")
	@PreAuthorize("hasAnyAuthority('course:edit')")
	public String edit(Principal principal,
	                   Model model,
	                   @PathVariable(name = "id") String id) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.COURSES)
				.orElseThrow();
		Course course = courseRepository.findById(Identifiable.getId(id))
				.orElseThrow();
		
		if (!course.getUsers().contains(user)) return "redirect:/home";
		
		model.addAttribute("allSets", wordSetRepository.findAll().stream()
				.map(v -> v.getName().replaceAll("[~@]", "") + "~" + v.getVisibleId())
				.collect(Collectors.joining("@")));
		model.addAttribute("user", user);
		model.addAttribute("course", course);
		model.addAttribute("selectedSetsRaw", course.getWordSetInTest().stream()
				.map(v -> v.getName().replaceAll("[~@]", "") + "~" + v.getVisibleId())
				.collect(Collectors.joining("@")));
		model.addAttribute("selectedSets", course.getWordSetInTest());
		
		return "courseEdit";
	}
	
	
	@PostMapping("course/{id}/edit")
	@PreAuthorize("hasAnyAuthority('course:edit')")
	public String edit(Principal principal,
	                   Model model,
	                   @PathVariable(name = "id") String id,
	                   @RequestParam(name = "message") String message) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.COURSES)
				.orElseThrow();
		Course course = courseRepository.findById(Identifiable.getId(id))
				.orElseThrow();
		
		if (!course.getUsers().contains(user)) return "redirect:/home";
		
		course.setCurrentNote(message);
		courseRepository.save(course);
		
		return "redirect:/course/{id}/";
	}
	
	@PostMapping("course/{id}/edit/add")
	@PreAuthorize("hasAnyAuthority('course:edit')")
	public String addSet(Principal principal,
	                     Model model,
	                     @PathVariable(name = "id") String id,
	                     @RequestBody String setID) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.COURSES)
				.orElseThrow();
		Course course = courseRepository.findById(Identifiable.getId(id))
				.orElseThrow();
		WordSet set = wordSetRepository.findById(Identifiable.getId(setID))
				.orElseThrow();
		
		if (!course.getUsers().contains(user)) return "redirect:/home";
		
		course.getWordSetInTest().add(set);
		courseRepository.save(course);
		
		return edit(principal, model, id);
	}
	
	@PostMapping("course/{id}/edit/remove")
	@PreAuthorize("hasAnyAuthority('course:edit')")
	public String removeSet(Principal principal,
	                        Model model,
	                        @PathVariable(name = "id") String id,
	                        @RequestBody String setID) {
		// TODO: Rewrite this without duplicated ode
		User user = controlService.getUser(principal, model, ControlService.MenuID.COURSES)
				.orElseThrow();
		Course course = courseRepository.findById(Identifiable.getId(id))
				.orElseThrow();
		WordSet set = wordSetRepository.findById(Identifiable.getId(setID))
				.orElseThrow();
		
		if (!course.getUsers().contains(user)) return "redirect:/home";
		
		course.getWordSetInTest().remove(set);
		courseRepository.save(course);
		
		return edit(principal, model, id);
	}
	
}
