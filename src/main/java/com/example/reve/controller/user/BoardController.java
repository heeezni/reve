package com.example.reve.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/board")
public class BoardController {

  @GetMapping("/notice/list")
  public String noticeList() {
    return "board/notice/list";
  }

  @GetMapping("/notice/detail")
  public String noticeDetail() {
    return "board/notice/detail";
  }

  @GetMapping("/qna/list")
  public String qnaList() {
    return "board/qna/list";
  }

  @GetMapping("/qna/detail")
  public String qnaDetail() {
    return "board/qna/detail";
  }

  @GetMapping("/qna/form")
  public String qnaForm() {
    return "board/qna/form";
  }
}
