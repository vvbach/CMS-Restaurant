package vn.tts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "system_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigEntity {
    @Id
    @Column(name = "config_key", length = 100)
    private String key;

    @Column(name = "config_value", length = 255)
    private String value;
}

