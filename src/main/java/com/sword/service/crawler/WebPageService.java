package com.sword.service.crawler;


import com.sword.domain.WebPage;
import com.sword.gsa.spis.scs.service.dto.WebPageDTO;


public interface WebPageService {

    WebPage convertWebPageDTOToWebPage(WebPageDTO webPageDTO);

    WebPageDTO convertWebPageToWebPageDTO(WebPage webPage);

    void addWebPage(WebPageDTO webPageDTO);

}