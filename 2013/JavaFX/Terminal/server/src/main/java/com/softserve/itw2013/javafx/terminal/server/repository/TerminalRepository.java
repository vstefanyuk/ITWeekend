package com.softserve.itw2013.javafx.terminal.server.repository;

import com.softserve.itw2013.javafx.terminal.data.Terminal;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 17.11.13
 * Time: 20:41
 * To change this template use File | Settings | File Templates.
 */
public interface TerminalRepository extends CrudRepository<Terminal, Integer>, JpaSpecificationExecutor<Terminal> {
    Terminal findOneByUuid(String uuid);
}
