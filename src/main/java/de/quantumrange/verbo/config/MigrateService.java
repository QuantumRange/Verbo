package de.quantumrange.verbo.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.quantumrange.verbo.model.*;
import de.quantumrange.verbo.service.repos.CourseRepository;
import de.quantumrange.verbo.service.repos.UserRepository;
import de.quantumrange.verbo.service.repos.WordRepository;
import de.quantumrange.verbo.service.repos.WordSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Component
public class MigrateService implements CommandLineRunner {
	
	private static final DateTimeFormatter SAVE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
	private static final Logger log = LoggerFactory.getLogger(MigrateService.class);
	
	private final UserRepository userRepository;
	private final WordSetRepository wordSetRepository;
	private final WordRepository wordRepository;
	private final CourseRepository courseRepository;
	private final ApplicationContext applicationContext;
	
	@Autowired
	public MigrateService(UserRepository userRepository,
	                      WordSetRepository wordSetRepository,
	                      WordRepository wordRepository,
	                      CourseRepository courseRepository,
	                      ApplicationContext applicationContext) {
		this.userRepository = userRepository;
		this.wordSetRepository = wordSetRepository;
		this.wordRepository = wordRepository;
		this.courseRepository = courseRepository;
		this.applicationContext = applicationContext;
	}
	
	@Override
	public void run(String... args) throws Exception {
		File wordsFile = new File("./voc.json");
		File wordSetFile = new File("./sets.json");
		File usersFile = new File("./users.json");
		File coursesFile = new File("./courses.json");
		
		File[] files = new File[]{
				wordsFile, wordSetFile, usersFile, coursesFile
		};
		
		// No migration needed
		if (Arrays.stream(files).noneMatch(File::exists)) return;
		
		// Need more old data
		if (!Arrays.stream(files).allMatch(File::exists)) {
			log.error("Not all files are present to start the migration! Make sure the following files are present: {}",
					Arrays.stream(files)
							.filter(file -> !file.exists())
							.map(File::getName)
							.collect(Collectors.joining(",")));
			
			// Stop program because migration can't start
			SpringApplication.exit(applicationContext);
		}
		
		ObjectMapper mapper = new JsonMapper();
		ObjectReader reader = mapper.reader();
		
		Map<Long, Long> userIdMap = importUsers(reader.readTree(new FileInputStream(usersFile)));
		Map<Long, Long> wordSetIdMap = importWords(reader.readTree(new FileInputStream(wordsFile)), reader.readTree(new FileInputStream(wordSetFile)), userIdMap);
		importCourses(reader.readTree(new FileInputStream(coursesFile)), wordSetIdMap, userIdMap);
		
		// "Deleting" data
		File migrateFolder = new File("./migrated/");
		
		migrateFolder.mkdirs();
		
		for (File file : files) {
			Files.move(file.toPath(), new File(migrateFolder, file.getName()).toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		}
		
		log.info("Successful migration!");
	}
	
	private Map<Long, Long> importUsers(JsonNode usersNode) {
		Map<Long, Long> userIdMap = new HashMap<>();
		
		for (JsonNode node : usersNode) {
			long id = node.get("id").asLong();
			
			String username = node.get("username").asText();
			String displayName = node.get("displayName").asText(username);
			String password = node.get("password").asText();
			String roleStr = node.get("role").asText();
			
			if (roleStr.equals("ROOT")) roleStr = Role.ADMIN.name();
			
			User user = userRepository.saveAndFlush(new User(0L,
					username,
					displayName,
					password,
					new ArrayList<>(),
					new HashSet<>(),
					new HashMap<>(),
					Role.valueOf(roleStr)));
			
			userIdMap.put(id, user.getId());
		}
		
		return userIdMap;
	}
	
	private Map<Long, Long> importWords(JsonNode wordsNode, JsonNode wordSetsNode, Map<Long, Long> userIdMap) {
		Map<Long, Long> wordSetIdMap = new HashMap<>();
		Map<Long, JsonNode> wordMap = new HashMap<>();
		
		for (JsonNode node : wordsNode) {
			long id = node.get("id").asLong();
			
			wordMap.put(id, node);
		}
		
		for (JsonNode node : wordSetsNode) {
			long id = node.get("id").asLong();
			
			String name = node.get("name").asText();
			long ownerId = node.get("owner").asLong();
			User owner = userRepository.findById(ownerId)
					.orElse(userRepository.findAll().get(0));
			String timestampStr = node.get("timestamp").asText();
			LocalDateTime timestamp = LocalDateTime.parse(timestampStr, SAVE_FORMAT);
			
			Language questionLanguage = Language.valueOf(wordsNode.get(0).get("questionLang").asText());
			Language answerLanguage = Language.valueOf(wordsNode.get(0).get("answerLang").asText());
			
			WordSet wordSet = wordSetRepository.saveAndFlush(new WordSet(
					0L,
					name,
					owner,
					new HashSet<>(),
					new HashSet<>(),
					questionLanguage,
					answerLanguage,
					EditPolicy.OWNER,
					timestamp,
					new HashSet<>()
			));
			
			for (JsonNode wordNode : node.get("vocabularies")) {
				long wordId = wordNode.asLong();
				JsonNode vocWordNode = wordMap.get(wordId);
				
				String question = vocWordNode.get("question").asText();
				String answer = vocWordNode.get("answer").asText();
				
				wordSet.getWords().add(wordRepository.saveAndFlush(new Word(
						0L,
						wordSet,
						question,
						answer
				)));
			}
			
			wordSetRepository.saveAndFlush(wordSet);
			wordSetIdMap.put(id, wordSet.getId());
		}
		
		return wordSetIdMap;
	}
	
	private void importCourses(JsonNode coursesNode, Map<Long, Long> wordSetIdMap, Map<Long, Long> userIdMap) {
		User root = userRepository.findByRole(Role.ADMIN).get(0);
		
		for (JsonNode node : coursesNode) {
			String name = node.get("name").asText();
			String currentNote = node.get("currentNote").asText().replaceAll("\n", " ");
			String code = node.get("code").asText();
			
			Course course = courseRepository.saveAndFlush(new Course(
					0L,
					name,
					root,
					new HashSet<>(),
					new HashSet<>(),
					currentNote,
					new HashSet<>(),
					code
			));
			
			for (JsonNode userNode : node.get("users")) {
				long userId = userNode.asLong();
				
				userRepository.findById(userIdMap.getOrDefault(userId, 0L))
						.ifPresent(user -> course.getUsers().add(user));
			}
			
			for (JsonNode setNode : node.get("sets")) {
				long setId = setNode.asLong();
				
				wordSetRepository.findById(wordSetIdMap.getOrDefault(setId, 0L))
						.ifPresent(wordSet -> course.getWordSets().add(wordSet));
			}
			
			for (JsonNode setNode : node.get("currentSets")) {
				long setId = setNode.asLong();
				
				wordSetRepository.findById(wordSetIdMap.getOrDefault(setId, 0L))
						.ifPresent(wordSet -> course.getWordSetInTest().add(wordSet));
			}
			
			courseRepository.saveAndFlush(course);
		}
	}
	
}
