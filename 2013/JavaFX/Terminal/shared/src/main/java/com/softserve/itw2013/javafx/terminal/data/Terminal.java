package com.softserve.itw2013.javafx.terminal.data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 17.11.13
 * Time: 20:13
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "terminal")
public class Terminal implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "terminal_seq")
    @SequenceGenerator(name = "terminal_seq", sequenceName = "terminal_seq")
    @Column(name = "terminal_id")
    private Integer id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
