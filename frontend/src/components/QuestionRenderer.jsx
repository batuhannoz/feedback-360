import React from 'react';
import { QuestionType } from '../models/enums/QuestionType';
import StarRating from './StarRating';

const QuestionRenderer = ({ question, value, onChange, disabled }) => {
    switch (question.type) {
        case QuestionType.SCALE_1_TO_5:
            return (
                <StarRating
                    rating={parseInt(value) || 0}
                    onRatingChange={onChange}
                    disabled={disabled}
                />
            );
        case QuestionType.YES_NO:
            return (
                <div className="flex items-center space-x-4">
                    <label className="flex items-center cursor-pointer">
                        <input
                            type="radio"
                            name={`question_${question.answerId}`}
                            value="YES"
                            checked={value === 'YES'}
                            onChange={() => onChange('YES')}
                            disabled={disabled}
                            className="mr-2"
                        />
                        Yes
                    </label>
                    <label className="flex items-center cursor-pointer">
                        <input
                            type="radio"
                            name={`question_${question.answerId}`}
                            value="NO"
                            checked={value === 'NO'}
                            onChange={() => onChange('NO')}
                            disabled={disabled}
                            className="mr-2"
                        />
                        No
                    </label>
                </div>
            );
        case QuestionType.TEXT:
        default:
            return (
                <textarea
                    className="w-full p-2 border rounded mt-2 bg-gray-50"
                    rows="4"
                    value={value || ''}
                    onChange={(e) => onChange(e.target.value)}
                    disabled={disabled}
                    required
                />
            );
    }
};

export default QuestionRenderer;
