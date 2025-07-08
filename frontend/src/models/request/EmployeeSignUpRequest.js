export class EmployeeSignUpRequest {
    constructor(invitationToken, password) {
        this.invitationToken = invitationToken;
        this.password = password;
    }
}
