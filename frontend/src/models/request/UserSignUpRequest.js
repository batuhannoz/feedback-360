export default class UserSignUpRequest {
    constructor(invitationToken, password) {
        this.invitationToken = invitationToken;
        this.password = password;
    }
}