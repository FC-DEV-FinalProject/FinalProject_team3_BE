package com.investmetic.global.util.stibee;


import com.investmetic.domain.user.dto.request.UserModifyDto;
import com.investmetic.domain.user.model.Role;
import com.investmetic.domain.user.model.entity.User;
import com.investmetic.global.util.stibee.client.AutoApiStibeeClient;
import com.investmetic.global.util.stibee.client.StibeeClient;
import com.investmetic.global.util.stibee.dto.object.EmailAndCode;
import com.investmetic.global.util.stibee.dto.request.SubscriberField;
import com.investmetic.global.util.stibee.dto.request.EmailSubscribe;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StibeeEmailService {

    // HttpInterface로 사용.
    private final StibeeClient stibeeClient;

    private final AutoApiStibeeClient autoApiStibeeClient;


    // 하나의 주소록 그룹 나눔. - 자동이메일 해당 주소록에서 하나만 생성.
    @Value("${stibee.email.address-book.default.address}")
    private int defaultAddressBook;

    @Value("${stibee.email.address-book.default.group.admin}")
    private String adminGroup;

    @Value("${stibee.email.address-book.default.group.investor}")
    private String investGroup;

    @Value("${stibee.email.address-book.default.group.trader}")
    private String traderGroup;



    /**
     * 주소록에 추가 - 회원 가입시, 탈퇴한 회원이 똑같은 이메일로 다시 회원가입 한 경우 이메일 발송 안함. SuperAdmin은 회원가입, 등급 변경 해도 안됨.
     *
     * @param user 주소록에 필요한 사용자 정보 필드가 추가될 수 있으므로 UserEntity를 받습니다.
     */
    public Boolean addSubscriber(User user) {

        List<String> groupIds = new ArrayList<>();

        // 트레이더인지 투자자인지 그룹으로 구분.
        groupIds.add(Role.isTrader(user.getRole()) ? traderGroup : investGroup);

        //이메일 수신 동의 -> 광고성 정보 수신 동의
        String adAgreed = Boolean.TRUE.equals(user.getInfoAgreement()) ? "Y" : "N";

        // 해당 회원 약관 동의 날짜 업데이트
        SubscriberField subscriber = SubscriberField.create(user.getEmail(), user.getUserName(), adAgreed);
        subscriber.updateTermDate();

        EmailSubscribe emailSubscribe = EmailSubscribe.toSubscriber(groupIds, subscriber);

        // 실패 이유는 SignupValue에서 찾을 수 있음.
        // 실제 이메일 발송되는 주소록에 저장.
        return stibeeClient.subscribe(defaultAddressBook, emailSubscribe).isOk();
    }

    /**
     * 이미등록된 회원의 정보를 변경.
     * <br>
     * - 현재는 광고성 정보 수신 내용만 변경. 해당 메서드로 그룹추가 되지만 api따로 있으니 분리하겠습니다.
     */
    public Boolean updateSubscriberInfo(UserModifyDto user, Role role) {

        List<String> groupIds = new ArrayList<>();

        groupIds.add(Role.isTrader(role) ? traderGroup : investGroup);

        if (Role.isAdmin(role)) {
            groupIds.add(adminGroup);
        }

        String adAgreed = Boolean.TRUE.equals(user.getInfoAgreement()) ? "Y" : "N";

        // 정보 변경시에는 약관 날짜 업데이트하지 않기.
        SubscriberField subscriber = SubscriberField.create(user.getEmail(), null, adAgreed);

        EmailSubscribe emailSubscribe = EmailSubscribe.toManual(groupIds, subscriber);

        return stibeeClient.subscribe(defaultAddressBook, emailSubscribe).isOk();
    }


    /**
     * 약관 날짜 업데이트
     */
    public Boolean updateSubscriberTermDate(String email) {

        SubscriberField subscriber = SubscriberField.create(email, null, null);
        //약관날짜 업데이트
        subscriber.updateTermDate();

        // groupids null로 보내도 그룹 취소안되긴 해서...
        EmailSubscribe emailSubscribe = EmailSubscribe.toManual(null, subscriber);

        return stibeeClient.subscribe(defaultAddressBook, emailSubscribe).isOk();
    }

    /**
     * 그룹 할당 - 회원 등급 변경시
     */
    public Boolean assignGroup(String email) {
        return stibeeClient.assignGroup(
                defaultAddressBook, Integer.parseInt(adminGroup), List.of(email)
        ).isOk();
    }

    /**
     * 그룹 취소
     */
    public Boolean releaseGroup(String email) {
        return stibeeClient.releaseGroup(
                defaultAddressBook, Integer.parseInt(adminGroup), List.of(email)
        ).isOk();
    }


    /**
     * 주소록에서 삭제 - 회원 탈퇴시
     */
    public Boolean deleteSubscriber(String email) {
        // restClient.delete()로는 body() 사용 못함. method(HttpMethod.DELETE)로 해야 가능함.
        return stibeeClient.deleteSubscriber(defaultAddressBook, List.of(email)).isOk();
    }


    /**
     * 이메일 수신 거부 - 주소록에서 수신 거부로 표시됨.
     *  - 수신 거부 취소할 때 addSubscriber사용하면 됨. 요청에 occuredBy subscriber로 설정하면 수신 거부 취소됨.
     */
    public Boolean unsubscribeEmail(String email) {
        return stibeeClient.unsubscribeEmail(defaultAddressBook, List.of(email)).isOk();
    }


    /**
     * 인증코드 발송 - 비밀번호 재설정 및 이메일 인증시
     *
     * @param email - 회원의 Email
     * @param code  - 인증코드.
     */
    public void sendAuthenticationCode(String email, String code) {

        EmailAndCode emailAndCode = new EmailAndCode(email, code);
//         성공시 그냥 ok 만 옴
//         실패시 "구독자 상태를 파악할 수 없습니다.", HttpClientErrorException&BadRequest -> 스티비 안에 해당 이메일이 없음.
//         response.getBody() -> inputStream
        autoApiStibeeClient.sendAuthenticationCode(emailAndCode);
    }


}
