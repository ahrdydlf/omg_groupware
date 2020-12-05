package com.omg.board.controller;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.gson.Gson;
import com.omg.attachment.domain.AttachmentVO;
import com.omg.attachment.service.AttachmentServiceImpl;
import com.omg.board.domain.BoardVO;
import com.omg.board.service.BoardService;
import com.omg.cmn.Message;
import com.omg.cmn.Search;
import com.omg.cmn.StringUtil;
import com.omg.code.dao.CodeDaoImpl;
import com.omg.code.domain.Code;
import com.omg.employee.domain.EmployeeVO;

@Controller
public class BoardController 
{
	final Logger LOG = LoggerFactory.getLogger(BoardController.class);
	
	@Autowired
	BoardService boardService;
	
	@Autowired
	CodeDaoImpl codeDaoImpl;
	
	@Autowired
	AttachmentServiceImpl attachmentService;
	
	@RequestMapping(value="board/board_main.do",method=RequestMethod.GET)
	public String board_main() 
	{
		LOG.debug("* http://localhost:8080/cmn/board/board_main.do *");
		
		
		return "board/board_main";
	}
	
	@RequestMapping(value="board/boardInsert.do",method=RequestMethod.GET)
	public String boardInsert(Model model, String div) 
	{
		LOG.debug("* http://localhost:8080/cmn/board/board_write.do *");
		
		model.addAttribute("boardDiv", div);
		
		
		return "board/board_write";
	}
	
	@RequestMapping(value="board/doInsert.do", method = RequestMethod.POST
			,produces = "application/json;charset=UTF-8"
			)
	@ResponseBody
	public String doInsert(MultipartHttpServletRequest multi) throws IllegalStateException, IOException
	{
		LOG.debug("=doInsert()=");
		//paramSet
		BoardVO boardVO = new BoardVO();
		boardVO.setDiv(multi.getParameter("div"));
		boardVO.setTitle(multi.getParameter("title"));
		boardVO.setContents(multi.getParameter("contents"));
		boardVO.setRegId(multi.getParameter("regId"));
		
		LOG.debug("===========================");
		LOG.debug("=doInsert()=");
		LOG.debug("=boardVO : "+boardVO);
		LOG.debug("===========================");
		
		int flag = this.boardService.doInsert(boardVO);
		LOG.debug("===========================");
		LOG.debug("=flag : "+flag);
		LOG.debug("===========================");
		
		//메세지 처리
		Message message = new Message();
		message.setMsgId(flag+"");
		
		if(flag==1)
		{
			String fileCode = boardVO.getFilecode();
			String dir = "board";
			List<AttachmentVO> list = StringUtil.fileUpload(multi, fileCode, dir);
			LOG.debug("업로드 파일 개수 = " + list.size());
			int fileFlag = 0;
			for(AttachmentVO vo : list) {
				fileFlag = attachmentService.doInsert(vo);
			}
			message.setMsgContents(" 게시글 등록이 완료 되었습니다.");
		}
		else
		{
			message.setMsgContents(" 게시글 등록에 실패 하였습니다.");
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(message);
		LOG.debug("===========================");
		LOG.debug("=json="+json);
		LOG.debug("===========================");
		
		return json;
		
	}
	
	@RequestMapping(value="board/doDelete.do", method = RequestMethod.POST
											 ,produces = "application/json;charset=UTF-8"
				   )
	@ResponseBody
	public String doDelete(BoardVO board)
	{
		LOG.debug("===========================");
		LOG.debug("=doDelete()=");
		LOG.debug("=board : "+board);
		LOG.debug("===========================");
		
		int flag = this.boardService.doDelete(board);
		LOG.debug("===========================");
		LOG.debug("=flag : "+flag);
		LOG.debug("===========================");
		
		//메세지 처리
		Message message = new Message();
		message.setMsgId(flag+"");
		
		if(flag==1)
		{
			message.setMsgContents(" 게시글 삭제가 완료 되었습니다.");
		}
		else
		{
			message.setMsgContents(" 게시글 삭제에 실패 하였습니다.");
		}
		
		Gson gson = new Gson();
        String json = gson.toJson(message);
        LOG.debug("===========================");
        LOG.debug("=json="+json);
        LOG.debug("===========================");
		
		return json;
	}
	
	@RequestMapping(value="board/doUpdate.do", method = RequestMethod.POST
			   								,produces = "application/json;charset=UTF-8"
				   )
	@ResponseBody
	public String doUpdate(MultipartHttpServletRequest multi, BoardVO board) throws IllegalStateException, IOException
	{
		LOG.debug("===========================");
		LOG.debug("=doUpdate()=");
		LOG.debug("=board : "+board);
		LOG.debug("===========================");
		
		int flag = this.boardService.doUpdate(board);
		LOG.debug("===========================");
		LOG.debug("=flag : "+flag);
		LOG.debug("===========================");
		
		Message message = new Message();
		message.setMsgId(String.valueOf(flag));
		
		if(flag==1)
		{
			String fileCode = board.getFilecode();
			String dir = "board";
			List<AttachmentVO> list = StringUtil.fileUpload(multi, fileCode, dir);
			LOG.debug("업로드 파일 개수 = " + list.size());
			int fileFlag = 0;
			if(list.size()>0) {
				attachmentService.doDelete(list.get(0));
				for(AttachmentVO vo : list) {
					fileFlag = attachmentService.doInsert(vo);
				}
			}
			
			message.setMsgContents(" 게시글 수정이 완료 되었습니다.");
		}
		else
		{
			message.setMsgContents(" 게시글 수정에 실패 하였습니다.");
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(message);
		LOG.debug("===========================");
		LOG.debug("=json="+json);
		LOG.debug("===========================");
		
		return json;
	}
	
	@RequestMapping(value="board/doSelectOne.do", method = RequestMethod.GET)
	public String doSelectOne(BoardVO boardVO, Locale locale,Model model,HttpSession session)
	{
		LOG.debug("===========================");
		LOG.debug("=doSelectOne()=");
		LOG.debug("=boardVO : "+boardVO);
		LOG.debug("===========================");
		
		if(0 == boardVO.getBoardSeq()) {
			throw new IllegalArgumentException("게시글 seq를 확인하세요");
		}
		EmployeeVO empVO = (EmployeeVO) session.getAttribute("employee");
		
		BoardVO outVO = this.boardService.doSelectOne(boardVO, empVO.getEmployee_id());
		model.addAttribute("vo", outVO);
		
		AttachmentVO inFileVO = new AttachmentVO();
		inFileVO.setFileCode(outVO.getFilecode());
		List<AttachmentVO> fileList = attachmentService.doSelectList(inFileVO);
		model.addAttribute("fileList",fileList);
		
		
		String returnUrl = "board/board_info";
		
		return returnUrl;
	}
	
	@RequestMapping(value="board/doSelectOneMng.do", method = RequestMethod.GET)
	public String doSelectOneMng(BoardVO boardVO, Locale locale,Model model,HttpSession session)
	{
		LOG.debug("===========================");
		LOG.debug("=doSelectOneMng()=");
		LOG.debug("=boardVO : "+boardVO);
		LOG.debug("===========================");
		
		if(0 == boardVO.getBoardSeq()) {
			throw new IllegalArgumentException("게시글 seq를 확인하세요");
		}
		EmployeeVO empVO = (EmployeeVO) session.getAttribute("employee");
		
		BoardVO outVO = this.boardService.doSelectOne(boardVO, empVO.getEmployee_id());
		model.addAttribute("vo", outVO);
		
		AttachmentVO inFileVO = new AttachmentVO();
		inFileVO.setFileCode(outVO.getFilecode());
		List<AttachmentVO> fileList = attachmentService.doSelectList(inFileVO);
		model.addAttribute("fileList",fileList);
		
		
		String returnUrl = "board/board_mng";
		
		return returnUrl;
	}
	
	@RequestMapping(value="board/doSelectList.do", method = RequestMethod.GET)
	public String doSelectList(Search search,Model model)
	{
		LOG.debug("===========================");
		LOG.debug("=doSelectList()=");
		LOG.debug("=search : "+search);
		LOG.debug("===========================");
		
		//페이지 num 기본값 처리
		if(search.getPageNum()==0)
		{
			search.setPageNum(1);
		}
		
		//페이지 사이즈 기본값 처리
		if(search.getPageSize()==0)
		{
			search.setPageSize(10);
		}
		
		//게시구분 초기화 : 공지사항(10), 자유게시판(20)
		if(search.getDiv() == null) 
		{
			search.setDiv("10");
		}
		
		//검색구분
		search.setSearchDiv(StringUtil.nvl(search.getSearchDiv(), ""));
		LOG.debug("[search.getSearchDiv()] : "+search.getSearchDiv());
		
		//검색어
		search.setSearchWord(StringUtil.nvl(search.getSearchWord(), ""));
		LOG.debug("[search.getSearchWord()] : "+search.getSearchWord());
		
		//board_list 화면으로 param 전달
		model.addAttribute("vo", search);
		
		List<BoardVO> boardList = this.boardService.doSelectList(search);
		model.addAttribute("list", boardList);
		
		//총글수
		int totalCnt = 0;
		
		if(boardList.size()>0)
		{
			BoardVO totalVO = boardList.get(0);
			totalCnt = totalVO.getTotalCnt();
		}
		model.addAttribute("totalCnt", totalCnt);
		
		for(BoardVO vo : boardList) {
			LOG.debug(vo.toString());
		}
		
		//code : PAGE_SIZE,BOARD_CONDITION
		String codeArray = "PAGE_SIZE,BOARD_CONDITION,BOARD_DIV";
		List<Code> list = codeDaoImpl.doSelectList(codeArray);
		
		List<Code> pageSizeList = StringUtil.getCodeSearch(list, "PAGE_SIZE");
		List<Code> boardConditionList = StringUtil.getCodeSearch(list, "BOARD_CONDITION");
		
		
		Code code = new Code();
		code.setMstCode("BOARD_DIV"); 
		code.setDetCode(search.getDiv());
		
		Code coVO = codeDaoImpl.doSelectOne(code);
		
		model.addAttribute("coVO", coVO);
		
		LOG.debug("===========================");
		LOG.debug("=code : "+code);
		LOG.debug("===========================");
		
		List<Code> boardDivlist = StringUtil.getCodeSearch(list, "BOARD_DIV");
		
		model.addAttribute("PAGE_SIZE", pageSizeList);
		model.addAttribute("BOARD_CONDITION", boardConditionList);
		
		model.addAttribute("BOARD_DIV", boardDivlist);
		
		//View 화면
		String view = "board/board_main";
		LOG.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		return view;
	}
}
