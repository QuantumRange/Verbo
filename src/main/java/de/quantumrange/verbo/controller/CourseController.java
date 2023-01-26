package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.*;
import de.quantumrange.verbo.service.*;
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
	private final UserService userService;
	private final CourseService courseService;
	private final VocSetService vocSetService;
	private final InviteService inviteService;
	
	@Autowired
	public CourseController(ControlService controlService,
	                        UserService userService,
	                        CourseService courseService,
	                        VocSetService vocSetService, InviteService inviteService) {
		this.controlService = controlService;
		this.userService = userService;
		this.courseService = courseService;
		this.vocSetService = vocSetService;
		this.inviteService = inviteService;
	}
	
	@GetMapping("courses")
	@PreAuthorize("hasAnyAuthority('site:courses')")
	public String courses(Principal principal,
	                      Model model,
	                      @RequestParam(name = "error", defaultValue = "-1") int code,
	                      @RequestParam(name = "all", defaultValue = "") String showAll) {
		Optional<User> user = controlService.getUser(principal, model, ControlService.MenuID.COURSES);
		
		if (user.get(MetaKey.FORCE_PASSWORD_CHANGE)) return "redirect:/myAccount";
		
		boolean all = showAll.equals("true") && user.hasPermission(Permission.COURSE_VIEW_ALL);
		
		model.addAttribute("all", all);
		
		List<Course> list = courseService.stream()
				.filter(c -> all || c.getUsers().contains(user.getId()))
				.toList();
		
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
		User user = userService.findByPrinciple(principal);
		
		name = name.replaceAll("[^A-Za-z\\sáéíóúàèìòùÁÉÍÓÚÀÈÌÒÙ\\-_+=:;<.>,?!@0-9%$#]", "");
		name = name.replaceFirst(" ", "");
		name = name.substring(0, Math.min(name.length(), 32));
		
		String finalName = name;
		
		if (name.isBlank() || courseService.parallel()
				.anyMatch(c -> c.getName().equalsIgnoreCase(finalName))) {
			response.sendRedirect("/courses?error=0");
			return;
		}
		
		Course course = new Course(courseService.generateID(),
				name,
				new HashSet<>(),
				new HashSet<>(),
				"No upcoming tests :)",
				new HashSet<>(),
				courseService.generateCode());
		
		course.getUsers().add(user.getId());
		
		courseService.insert(course);
		
		response.sendRedirect("/course/" + course.getVisibleId() + "/");
	}
	
	@PostMapping("course/join")
	@PreAuthorize("hasAnyAuthority('course:join')")
	public void join(HttpServletResponse response,
	                 Principal principal,
	                 @RequestParam(name = "code") String code) throws IOException {
		User user = userService.findByPrinciple(principal);
		
		Optional<Course> optional = courseService.findByCode(code);
		
		if (optional.isEmpty()) {
			response.sendRedirect("/courses?error=1");
			return;
		}
		
		Course course = optional.get();
		
		if (!course.getUsers().contains(user.getId())) {
			course.getUsers().add(user.getId());
			
			courseService.update(course);
		}
		
		response.sendRedirect("/course/" + course.getVisibleId() + "/");
	}
	
	@GetMapping("course/{id}/")
	@PreAuthorize("hasAnyAuthority('site:course')")
	public String course(Principal principal,
	                     Model model,
	                     @PathVariable(name = "id") String id) {
		Optional<User> user = controlService.getUser(principal, model, ControlService.MenuID.COURSES);
		if (user.get(MetaKey.FORCE_PASSWORD_CHANGE)) return "redirect:/myAccount";
		
		Course course = courseService.findByID(Identifiable.getId(id))
				.orElseThrow(() -> new IllegalArgumentException("Illegal course"));
		
		if (!course.getUsers().contains(user.getId())) return "redirect:/home";
		
		model.addAttribute("user", user);
		model.addAttribute("course", course);
		model.addAttribute("sets", controlService.getSets(user, course.getVocabularies())
				.toList());
		model.addAttribute("currentSets", controlService.getSets(user, course.getWordSetInTest())
				.toList());
		
		return "course";
	}
	
	@GetMapping("course/{id}/edit")
	@PreAuthorize("hasAnyAuthority('course:edit')")
	public String edit(Principal principal,
	                   Model model,
	                   @PathVariable(name = "id") String id) {
		Optional<User> user = controlService.getUser(principal, model, ControlService.MenuID.COURSES);
		Course course = courseService.findByID(Identifiable.getId(id))
				.orElseThrow(() -> new IllegalArgumentException("Illegal course"));
		if (!course.getUsers().contains(user.getId())) return "redirect:/home";
		
		model.addAttribute("allSets", vocSetService.stream()
				.map(v -> v.getName().replaceAll("[~@]", "") + "~" + v.getVisibleId())
				.collect(Collectors.joining("@")));
		model.addAttribute("user", user);
		model.addAttribute("course", course);
		model.addAttribute("selectedSetsRaw", vocSetService.stream()
				.filter(v -> course.getWordSetInTest().contains(v.getId()))
				.map(v -> v.getName().replaceAll("[~@]", "") + "~" + v.getVisibleId())
				.collect(Collectors.joining("@")));
		model.addAttribute("selectedSets", vocSetService.stream()
				.filter(v -> course.getWordSetInTest().contains(v.getId()))
				.collect(Collectors.toList()));
		
		return "courseEdit";
	}
	
	
	@PostMapping("course/{id}/edit")
	@PreAuthorize("hasAnyAuthority('course:edit')")
	public String edit(Principal principal,
	                   Model model,
	                   @PathVariable(name = "id") String id,
	                   @RequestParam(name = "message") String message) {
		Optional<User> user = controlService.getUser(principal, model, ControlService.MenuID.COURSES);
		Course course = courseService.findByID(Identifiable.getId(id))
				.orElseThrow(() -> new IllegalArgumentException("Illegal course"));
		if (!course.getUsers().contains(user.getId())) return "redirect:/home";
		
		course.setCurrentNote(message);
		courseService.update(course);
		
		return "redirect:/course/{id}/";
	}
	
	@PostMapping("course/{id}/edit/add")
	@PreAuthorize("hasAnyAuthority('course:edit')")
	public String addSet(Principal principal,
	                     Model model,
	                     @PathVariable(name = "id") String id,
	                     @RequestBody String setID) {
		Optional<User> user = controlService.getUser(principal, model, ControlService.MenuID.COURSES);
		Course course = courseService.findByID(Identifiable.getId(id))
				.orElseThrow();
		WordSet set = vocSetService.findByID(Identifiable.getId(setID))
				.orElseThrow();
		
		if (!course.getUsers().contains(user.getId())) return "redirect:/home";
		
		course.getWordSetInTest().add(set.getId());
		courseService.update(course);
		
		return edit(principal, model, id);
	}
	
	@PostMapping("course/{id}/edit/remove")
	@PreAuthorize("hasAnyAuthority('course:edit')")
	public String removeSet(Principal principal,
	                        Model model,
	                        @PathVariable(name = "id") String id,
	                        @RequestBody String setID) {
		Optional<User> user = controlService.getUser(principal, model, ControlService.MenuID.COURSES);
		Course course = courseService.findByID(Identifiable.getId(id))
				.orElseThrow();
		WordSet set = vocSetService.findByID(Identifiable.getId(setID))
				.orElseThrow();
		
		if (!course.getUsers().contains(user.getId())) return "redirect:/home";
		
		course.getWordSetInTest().remove(set.getId());
		courseService.update(course);
		
		return edit(principal, model, id);
	}
	
}
