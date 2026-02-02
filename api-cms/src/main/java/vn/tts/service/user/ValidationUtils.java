package vn.tts.service.user;

import vn.tts.entity.UserEntity;
import vn.tts.enums.DeleteEnum;
import vn.tts.exception.UserNotFoundException;

public class ValidationUtils {
    private ValidationUtils() {}

    public static void checkUserDelete(UserEntity user) {
        if (user.getIsDelete() == DeleteEnum.YES) {
            throw new UserNotFoundException("message.user.is.delete");
        }
    }
}
