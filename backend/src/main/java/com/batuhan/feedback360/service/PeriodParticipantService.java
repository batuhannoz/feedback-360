package com.batuhan.feedback360.service;

import com.batuhan.feedback360.config.AuthenticationPrincipalResolver;
import com.batuhan.feedback360.model.converter.UserConverter;
import com.batuhan.feedback360.model.entitiy.Company;
import com.batuhan.feedback360.model.entitiy.EvaluationPeriod;
import com.batuhan.feedback360.model.entitiy.PeriodParticipant;
import com.batuhan.feedback360.model.entitiy.User;
import com.batuhan.feedback360.model.response.ApiResponse;
import com.batuhan.feedback360.model.response.ParticipantResponse;
import com.batuhan.feedback360.repository.CompanyRepository;
import com.batuhan.feedback360.repository.EvaluationAssignmentRepository;
import com.batuhan.feedback360.repository.EvaluationPeriodRepository;
import com.batuhan.feedback360.repository.PeriodParticipantRepository;
import com.batuhan.feedback360.repository.UserRepository;
import com.batuhan.feedback360.util.MessageHandler;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PeriodParticipantService {

    private final EvaluationPeriodRepository evaluationPeriodRepository;
    private final AuthenticationPrincipalResolver principalResolver;
    private final UserConverter userConverter;
    private final MessageHandler messageHandler;
    private final PeriodParticipantRepository periodParticipantRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;

    public ApiResponse<List<ParticipantResponse>> getParticipantsByPeriod(Integer periodId) {
        if (!evaluationPeriodRepository.existsByIdAndCompanyId(periodId, principalResolver.getCompanyId())) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }
        List<PeriodParticipant> participants = periodParticipantRepository.findAllByPeriod_Id(periodId);

        List<ParticipantResponse> response = participants.stream()
            .map(PeriodParticipant::getEvaluatedUser)
            .map(userConverter::toParticipantResponse)
            .collect(Collectors.toList());

        return ApiResponse.success(response, messageHandler.getMessage("evaluation-period.participants.get.success"));
    }

    public ApiResponse<ParticipantResponse> addParticipantToPeriod(Integer periodId, Integer userId) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        Optional<EvaluationPeriod> period = evaluationPeriodRepository.findByIdAndCompany(periodId, company);
        if (period.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.not-found"));
        }
        Optional<User> user = userRepository.findByIdAndCompany(userId, company);
        if (user.isEmpty()) {
            return ApiResponse.failure(messageHandler.getMessage("user.not-found"));
        }
        if (periodParticipantRepository.existsByPeriod_IdAndEvaluatedUser_Id(periodId, userId)) {
            return ApiResponse.failure(messageHandler.getMessage("evaluation-period.participant.already-exists"));
        }
        PeriodParticipant newParticipant = PeriodParticipant.builder()
            .period(period.get())
            .evaluatedUser(user.get())
            .build();

        periodParticipantRepository.save(newParticipant);

        ParticipantResponse response = userConverter.toParticipantResponse(user.get());
        return ApiResponse.success(response, messageHandler.getMessage("evaluation-period.participant.add.success"));
    }

    @Transactional
    public ApiResponse<ParticipantResponse> deleteParticipantFromPeriod(Integer periodId, Integer userId) {
        Company company = companyRepository.getReferenceById(principalResolver.getCompanyId());
        Optional<PeriodParticipant> participant = periodParticipantRepository.findByPeriod_IdAndEvaluatedUser_Id(periodId, userId);
        if (participant.isEmpty() || !participant.get().getPeriod().getCompany().equals(company)) {
            return ApiResponse.failure(messageHandler.getMessage("period.participant.not-found"));
        }
        evaluationAssignmentRepository.deleteAllByPeriodParticipant(participant.get());
        periodParticipantRepository.delete(participant.get());
        ParticipantResponse response = userConverter.toParticipantResponse(participant.get().getEvaluatedUser());
        return ApiResponse.success(response, messageHandler.getMessage("evaluation-period.participant.delete.success"));
    }
}
