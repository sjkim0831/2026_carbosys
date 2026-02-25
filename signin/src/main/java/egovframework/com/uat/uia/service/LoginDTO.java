package egovframework.com.uat.uia.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {

    private String id;
    private String name;
    private String password;
    private String ihidNum;
    private String email;
    private String userSe;
    private String orgnztId;
    private String uniqId;
    private String ip;
    private String authorCode;

    // Auth info
    private String authTy;
    private String authDn;
    private String authCi;
    private String authDi;

    public LoginDTO(String id, String name, String password, String ihidNum, String email, String userSe,
            String orgnztId,
            String uniqId, String ip, String authorCode) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.ihidNum = ihidNum;
        this.email = email;
        this.userSe = userSe;
        this.orgnztId = orgnztId;
        this.uniqId = uniqId;
        this.ip = ip;
        this.authorCode = authorCode;
    }

}
