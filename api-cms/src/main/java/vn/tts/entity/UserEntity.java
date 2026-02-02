package vn.tts.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import vn.tts.enums.GenderEnum;
import vn.tts.enums.UserStatusEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uq_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uq_users_phone", columnNames = "phone")
        }
)
@Audited
@AuditTable(value = "users_aud")
@SQLDelete(sql = "update users set is_delete = 1 where id = ?")
public class UserEntity extends BaseEntity {
    @Column(nullable = false, length = 100, name = "username")
    private String username;

    @Column(nullable = false, length = 1000, name = "password")
    private String password;

    @Column(nullable = false, length = 200, name = "fullname")
    private String fullName;

    @Column(nullable = false, length = 20, name = "phone")
    public String phone;

    @Column(nullable = false, length = 100, name="email")
    public String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, name = "gender")
    public GenderEnum gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20,  name = "status")
    public UserStatusEnum status;

    @Column(length = 500, name = "avatar")
    public String avatar;

    @NotAudited
    @Column(nullable = false, columnDefinition = "smallint", name = "force_change_password")
    public int forceChangePassword;

    @Override
    protected void onCreate() {
        super.onCreate();
        status = UserStatusEnum.ACTIVE;
        forceChangePassword = 0;
    }
}
