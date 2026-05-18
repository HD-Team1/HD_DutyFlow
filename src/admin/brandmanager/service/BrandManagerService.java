package admin.brandmanager.service;

import admin.brandmanager.dao.BrandManagerDao;
import admin.brandmanager.dto.BrandManager;
import exception.AuthenticationException;
import exception.ErrorCode;
import exception.ValidationException;

public class BrandManagerService {

    private final BrandManagerDao brandManagerDAO = new BrandManagerDao();

    public BrandManager login(int managerId, String password) {

        if (managerId <= 0) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }

        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }

        BrandManager brandManager = brandManagerDAO.findByManagerId(managerId);

        if (brandManager == null) {
            throw new AuthenticationException(ErrorCode.UNAUTHORIZED);
        }

        if (!brandManager.authenticate(password)) {
            throw new AuthenticationException(ErrorCode.UNAUTHORIZED);
        }

        return brandManager;
    }
}