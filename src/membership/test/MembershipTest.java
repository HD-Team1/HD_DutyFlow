package membership.test;

import java.math.BigDecimal;
import java.time.LocalDate;

import common.Grade;
import membership.MembershipDAO;
import membership.MembershipService;

public class MembershipTest {

    public static void main(String[] args) {

        MembershipDAO dao = new MembershipDAO();
        MembershipService service = new MembershipService();

        testUpdateGrade(dao, service, 9001, Grade.SILVER, "주문 없음 → SILVER");
        testUpdateGrade(dao, service, 9002, Grade.GOLD, "800달러 → GOLD");
        testUpdateGrade(dao, service, 9003, Grade.BLACK, "2000달러 → BLACK");
        testUpdateGrade(dao, service, 9004, Grade.PRESTIGE, "4000달러 → PRESTIGE");
        testUpdateGrade(dao, service, 9005, Grade.SILVER, "CANCELED 제외 → SILVER");

        testUpdateIfExpired(dao, service, 9006, Grade.SILVER, "1년 미만 로그인 갱신 안 됨");
    }

    private static void testUpdateGrade(
            MembershipDAO dao,
            MembershipService service,
            int memberId,
            Grade expectedGrade,
            String testName
    ) {
        System.out.println("\n===== " + testName + " =====");

        BigDecimal amount = dao.sumPurchaseAmountLastOneYear(memberId);
        Grade beforeGrade = dao.findGradeByMemberId(memberId);
        LocalDate beforeDate = dao.findGradeSelectionDate(memberId);

        System.out.println("최근 1년 구매 금액: " + amount);
        System.out.println("갱신 전 등급: " + beforeGrade);
        System.out.println("갱신 전 선정일: " + beforeDate);

        service.updateMembershipGrade(memberId);

        Grade afterGrade = dao.findGradeByMemberId(memberId);
        LocalDate afterDate = dao.findGradeSelectionDate(memberId);

        System.out.println("갱신 후 등급: " + afterGrade);
        System.out.println("갱신 후 선정일: " + afterDate);
        System.out.println("예상 등급: " + expectedGrade);

        printResult(afterGrade == expectedGrade);
    }

    private static void testUpdateIfExpired(
            MembershipDAO dao,
            MembershipService service,
            int memberId,
            Grade expectedGrade,
            String testName
    ) {
        System.out.println("\n===== " + testName + " =====");

        BigDecimal amount = dao.sumPurchaseAmountLastOneYear(memberId);
        Grade beforeGrade = dao.findGradeByMemberId(memberId);
        LocalDate beforeDate = dao.findGradeSelectionDate(memberId);

        System.out.println("최근 1년 구매 금액: " + amount);
        System.out.println("로그인 갱신 전 등급: " + beforeGrade);
        System.out.println("로그인 갱신 전 선정일: " + beforeDate);

        Grade afterGrade = dao.findGradeByMemberId(memberId);
        LocalDate afterDate = dao.findGradeSelectionDate(memberId);

        System.out.println("로그인 갱신 후 등급: " + afterGrade);
        System.out.println("로그인 갱신 후 선정일: " + afterDate);
        System.out.println("예상 등급: " + expectedGrade);

        printResult(afterGrade == expectedGrade);
    }

    private static void printResult(boolean result) {
        if (result) {
            System.out.println("테스트 성공");
        } else {
            System.out.println("테스트 실패");
        }
    }
}