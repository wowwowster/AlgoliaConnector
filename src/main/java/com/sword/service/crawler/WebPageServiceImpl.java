package com.sword.service.crawler;

import com.sword.domain.WebPage;
import com.sword.repos.WebPageRepository;
import com.sword.gsa.spis.scs.service.dto.WebPageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WebPageServiceImpl implements WebPageService {

    @Autowired
    private WebPageRepository webPageRepository;

    //TODO à finir
    @Override
    public WebPage convertWebPageDTOToWebPage(WebPageDTO webPageDTO) {
        WebPage webPage = new WebPage();
        // TODO vérifier si logique
        webPage.setUpdatedAt(LocalDateTime.now());
        webPage.setUrl(webPageDTO.getUrl());
        return webPage;
    }

    //TODO à finir
    @Override
    public WebPageDTO convertWebPageToWebPageDTO(WebPage webPage) {
        WebPageDTO webPageDTO = new WebPageDTO();
        webPageDTO.setUrl(webPage.getUrl());
        return webPageDTO;
    }

    @Override
    public void addWebPage(WebPageDTO webPageDTO) {

        webPageRepository.save(convertWebPageDTOToWebPage(webPageDTO));

    }

}