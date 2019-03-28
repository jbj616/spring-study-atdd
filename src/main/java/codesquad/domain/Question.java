package codesquad.domain;

import codesquad.CannotDeleteException;
import codesquad.UnAuthorizedException;
import codesquad.AlreadyDeletedException;
import org.hibernate.annotations.Where;
import support.domain.AbstractEntity;
import support.domain.UrlGeneratable;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Question extends AbstractEntity implements UrlGeneratable {
    @Size(min = 3, max = 100)
    @Column(length = 100, nullable = false)
    private String title;

    @Size(min = 3)
    @Lob
    private String contents;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_question_writer"))
    private User writer;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @Where(clause = "deleted = false")
    @OrderBy("id ASC")
    private List<Answer> answers = new ArrayList<>();

    private boolean deleted = false;

    public Question() {
    }

    public Question(String title, String contents) {
        this.title = title;
        this.contents = contents;
    }

    public String getTitle() {
        return title;
    }

    public Question setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getContents() {
        return contents;
    }

    public Question setContents(String contents) {
        this.contents = contents;
        return this;
    }

    public User getWriter() {
        return writer;
    }

    public void writeBy(User loginUser) {
        this.writer = loginUser;
    }

    public void addAnswer(Answer answer) {
        answer.toQuestion(this);
        answers.add(answer);
    }

    public Question update(User user, Question question) {
        if (!writer.equals(user)) {
            throw new UnAuthorizedException();
        }
        this.title = question.title;
        this.contents = question.contents;
        return this;
    }

    public List<DeleteHistory> delete(User loginUser) throws Exception {
        if (!writer.equals(loginUser)) {
            throw new CannotDeleteException("다른 사용자의 질문 삭제 불가");
        }
        if (!hasSameWriterAnswers()) {
            throw new CannotDeleteException("질문의 답변 삭제 권한이 없습니다.");
        }
        if (this.deleted == true) {
            throw new AlreadyDeletedException("이미 삭제된 질문");
        }
        this.deleted = true;
        List<DeleteHistory> histories = new ArrayList<>();
        histories.add(new DeleteHistory(ContentType.QUESTION, getId(), loginUser, LocalDateTime.now()));
        for (Answer answer : answers) {
            histories.add(answer.delete(loginUser));
        }
        return histories;
    }

    public boolean isOwner(User loginUser) {
        return writer.equals(loginUser);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean hasNoAnswers(User user) {
        if (answers.size() == 0) {
            return true;
        } else
            return false;
    }

    public boolean hasSameWriterAnswers() {
        return answers.stream().allMatch(answer -> answer.getWriter().equals(writer));
    }

    @Override
    public String generateUrl() {
        return String.format("/questions/%d", getId());
    }

    @Override
    public String toString() {
        return "Question [id=" + getId() + ", title=" + title + ", contents=" + contents + ", writer=" + writer + "]";
    }

}
