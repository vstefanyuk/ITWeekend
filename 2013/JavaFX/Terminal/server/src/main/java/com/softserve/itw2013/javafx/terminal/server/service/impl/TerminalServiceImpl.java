package com.softserve.itw2013.javafx.terminal.server.service.impl;

import com.softserve.itw2013.javafx.terminal.data.Terminal;
import com.softserve.itw2013.javafx.terminal.server.repository.TerminalRepository;
import com.softserve.itw2013.javafx.terminal.server.service.TerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 17.11.13
 * Time: 20:37
 * To change this template use File | Settings | File Templates.
 */
@Transactional(readOnly = true)
@Service("terminalService")
public class TerminalServiceImpl implements TerminalService {
    @Autowired
    private TerminalRepository terminalRepository;

    @Override
    public List<Terminal> getAllTerminals() {
        return new ArrayList<Terminal>(terminalRepository.findAll((Specification<Terminal>) null));
    }
}
