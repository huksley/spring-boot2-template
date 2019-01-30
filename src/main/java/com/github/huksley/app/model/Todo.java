package com.github.huksley.app.model;

import java.util.Calendar;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import com.github.huksley.app.BaseEntity;
import lombok.*;
import org.hibernate.validator.constraints.Length;

/**
 * Todo model and entity
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Table(name = "test_todo")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(value = { "version", "created", "updated" }, ignoreUnknown = true)
@XmlRootElement(namespace = "http://github.io/xml/myapp/todo")
@JacksonXmlRootElement(namespace = "http://github.io/myapp/todo")
public class Todo extends BaseEntity {

    /**
     * Is it was done?
     */
    @NotNull
    private Boolean done;

    /**
     * Date when the todo have been completed.
     */
    @Column(name = "done_time")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ssZZZ", timezone = "UTC")
    private Calendar doneTime;

    /**
     * Description of things to be done.
     */
    @NotNull
    @Length(min = 1, max = 200)
    private String description;
}
