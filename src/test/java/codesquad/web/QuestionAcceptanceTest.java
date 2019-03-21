package codesquad.web;

import codesquad.domain.Question;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import support.HtmlFormDataBuilder;
import support.test.AcceptanceTest;

import static org.assertj.core.api.Assertions.assertThat;

public class QuestionAcceptanceTest extends AcceptanceTest {

	private HttpEntity<MultiValueMap<String, Object>> request() {
		return HtmlFormDataBuilder.urlEncodedForm().build();
	}

	private HttpEntity<MultiValueMap<String, Object>> request(String title, String contents) {
		return HtmlFormDataBuilder.urlEncodedForm()
				.addParameter("title", title)
				.addParameter("contents", contents)
				.build();
	}

	@Test
	public void form_no_login() {
		ResponseEntity<String> response = template().getForEntity("/questions/form", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	public void form() {
		ResponseEntity<String> response = basicAuthTemplate().getForEntity("/questions/form", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void post_no_login() {
		HttpEntity<MultiValueMap<String, Object>> request = request("제목테스트", "내용테스트");

		ResponseEntity<String> response = template().postForEntity("/questions", request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	public void post_login() {
		HttpEntity<MultiValueMap<String, Object>> request = request("제목테스트", "내용테스트");

		ResponseEntity<String> response = basicAuthTemplate().postForEntity("/questions", request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		assertThat(response.getHeaders().getLocation().getPath()).startsWith("/");
	}

	@Test
	public void list() {
		ResponseEntity<String> response = template().getForEntity("/", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void show() {
		Question question = defaultQuestion();
		ResponseEntity<String> response = template()
				.getForEntity(String.format("/questions/%d", question.getId()), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().contains(question.getContents())).isTrue();
	}

	@Test
	public void updateForm_no_login() {
		Question question = defaultQuestion();
		ResponseEntity<String> response = template()
				.getForEntity(String.format("/questions/%d/form", question.getId()), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	public void updateForm_login() {
		Question question = defaultQuestion();
		ResponseEntity<String> response = basicAuthTemplate()
				.getForEntity(String.format("/questions/%d/form", question.getId()), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void update_no_login() {
		Question question = defaultQuestion();
		HttpEntity<MultiValueMap<String, Object>> request = request("제목테스트", "내용테스트");

		ResponseEntity<String> response = template()
				.exchange(String.format("/questions/%d", question.getId()), HttpMethod.PUT, request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	public void update_login() {
		Question question = defaultQuestion();
		HttpEntity<MultiValueMap<String, Object>> request = request("제목테스트", "내용테스트");

		ResponseEntity<String> response = basicAuthTemplate()
				.exchange(String.format("/questions/%d", question.getId()), HttpMethod.PUT, request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		assertThat(response.getHeaders().getLocation().getPath()).startsWith("/");
	}

	@Test
	public void delete_no_login() {
		Question question = defaultQuestion();

		ResponseEntity<String> response = template()
				.exchange(String.format("/questions/%d", question.getId()), HttpMethod.DELETE, request(), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	public void delete_login() {
		Question question = defaultQuestion();

		ResponseEntity<String> response = basicAuthTemplate()
				.exchange(String.format("/questions/%d", question.getId()), HttpMethod.DELETE, request(), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		assertThat(response.getHeaders().getLocation().getPath()).startsWith("/");
	}
}