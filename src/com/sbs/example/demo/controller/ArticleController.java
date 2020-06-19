package com.sbs.example.demo.controller;

import java.util.List;

import com.sbs.example.demo.dto.Article;
import com.sbs.example.demo.dto.ArticleReply;
import com.sbs.example.demo.dto.Board;
import com.sbs.example.demo.dto.Member;
import com.sbs.example.demo.factory.Factory;
import com.sbs.example.demo.service.ArticleService;
import com.sbs.example.demo.service.MemberService;

public class ArticleController extends Controller {
	private ArticleService articleService;
	private MemberService memberService;

	public ArticleController() {
		articleService = Factory.getArticleService();
		memberService = Factory.getMemberService();
	}

	public void doAction(Request reqeust) {
		if (reqeust.getActionName().equals("list")) {
			actionList(reqeust);
		} else if (reqeust.getActionName().equals("write")) {
			actionWrite(reqeust);
		} else if (reqeust.getActionName().equals("changeBoard")) {
			actionChangeBoard(reqeust);
		} else if (reqeust.getActionName().equals("currentBoard")) {
			actionCurrentBoard(reqeust);
		} else if (reqeust.getActionName().equals("detail")) {
			actionDetail(reqeust);
		}
	}

	private void actionDetail(Request reqeust) {
		int id;
		try {
			id = Integer.parseInt(reqeust.getArg1());
		} catch (NumberFormatException e) {
			System.out.println("게시물 번호를 숫자로 입력해주세요.");
			return;
		}

		Article article = articleService.getArticle(id);

		if (article == null) {
			System.out.println("해당 게시물은 존재하지 않습니다.");
			return;
		}
		
		int writerId = article.getMemberId();
		Member member = memberService.getMember(writerId);
		String writerName = member.getName();
		
		List<ArticleReply> articleReplies = articleService.getArticleRepliesByArticleId(article.getId());
		int repliesCount = articleReplies.size();
		
		System.out.printf("== %d번 게시물 상세 시작 ==\n", article.getId());
		System.out.printf("번호 : %d\n", article.getId());
		System.out.printf("작성날짜 : %s\n", article.getRegDate());
		System.out.printf("제목 : %s\n", article.getTitle());
		System.out.printf("내용 : %s\n", article.getBody());
		System.out.printf("작성자번호 : %s\n", writerName);
		System.out.printf("댓글개수 : %d\n", repliesCount);
		
		for ( ArticleReply articleReply : articleReplies ) {
			Member replyWriter = memberService.getMember(articleReply.getMemberId());
			String replyWriterName = replyWriter.getName();
			
			System.out.printf("%d번 댓글 : %s by %s\n", articleReply.getId(), articleReply.getBody(), replyWriterName);
		}
		
		System.out.printf("== %d번 게시물 상세 끝 ==\n", article.getId());
	}

	private void actionCurrentBoard(Request reqeust) {
		Board board = Factory.getSession().getCurrentBoard();
		System.out.printf("현재 게시판 : %s\n", board.getName());
	}

	private void actionChangeBoard(Request reqeust) {
		String boardCode = reqeust.getArg1();

		Board board = articleService.getBoardByCode(boardCode);

		if (board == null) {
			System.out.println("해당 게시판이 존재하지 않습니다.");
		} else {
			System.out.printf("%s 게시판으로 변경되었습니다.\n", board.getName());
			Factory.getSession().setCurrentBoard(board);
		}
	}

	private void actionList(Request reqeust) {
		Board currentBoard = Factory.getSession().getCurrentBoard();
		List<Article> articles = articleService.getArticlesByBoardCode(currentBoard.getCode());

		System.out.printf("== %s 게시물 리스트 시작 ==\n", currentBoard.getName());
		for (Article article : articles) {
			System.out.printf("%d, %s, %s\n", article.getId(), article.getRegDate(), article.getTitle());
		}
		System.out.printf("== %s 게시물 리스트 끝 ==\n", currentBoard.getName());
	}

	private void actionWrite(Request reqeust) {
		System.out.printf("제목 : ");
		String title = Factory.getScanner().nextLine();
		System.out.printf("내용 : ");
		String body = Factory.getScanner().nextLine();

		// 현재 게시판 id 가져오기
		int boardId = Factory.getSession().getCurrentBoard().getId();

		// 현재 로그인한 회원의 id 가져오기
		int memberId = Factory.getSession().getLoginedMember().getId();
		int newId = articleService.write(boardId, memberId, title, body);

		System.out.printf("%d번 글이 생성되었습니다.\n", newId);
	}
}