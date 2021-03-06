package next.service;

import java.util.List;

import next.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import next.CannotOperateException;
import next.dao.AnswerDao;
import next.model.Answer;
import next.model.Question;
import next.model.User;

@Service
public class QnaService {
	@Autowired
	private QuestionRepository questionDao;
	@Autowired
	private AnswerDao answerDao;

	public Question findById(long questionId) {
		return questionDao.findOne(questionId);
	}

	public List<Answer> findAllByQuestionId(long questionId) {
		return answerDao.findAllByQuestionId(questionId);
	}

	public void deleteQuestion(long questionId, User user) throws CannotOperateException {
		Question question = questionDao.findOne(questionId);
		if (question == null) {
			throw new EmptyResultDataAccessException("존재하지 않는 질문입니다.", 1);
		}

		if (!question.isSameUser(user)) {
			throw new CannotOperateException("다른 사용자가 쓴 글을 삭제할 수 없습니다.");
		}

		List<Answer> answers = answerDao.findAllByQuestionId(questionId);
		if (answers.isEmpty()) {
			questionDao.delete(questionId);
			return;
		}

		boolean canDelete = true;
		for (Answer answer : answers) {
			User writer = question.getWriter();
			if (!writer.getUserId().equals(answer.getWriter())) {
				canDelete = false;
				break;
			}
		}

		if (!canDelete) {
			throw new CannotOperateException("다른 사용자가 추가한 댓글이 존재해 삭제할 수 없습니다.");
		}

		questionDao.delete(questionId);
	}

	public void updateQuestion(long questionId, Question newQuestion, User user) throws CannotOperateException {
		Question question = questionDao.findOne(questionId);
        if (question == null) {
        	throw new EmptyResultDataAccessException("존재하지 않는 질문입니다.", 1);
        }
        
        if (!question.isSameUser(user)) {
            throw new CannotOperateException("다른 사용자가 쓴 글을 수정할 수 없습니다.");
        }
        
        question.update(newQuestion);
        questionDao.save(question);
	}
}
