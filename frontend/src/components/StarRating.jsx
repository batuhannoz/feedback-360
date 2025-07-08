import React, { useState } from 'react';
import StarIcon from '../assets/icons/StarIcon';

const StarRating = ({ rating, onRatingChange, disabled }) => {
    const [hover, setHover] = useState(0);

    return (
        <div className="flex items-center">
            {[...Array(5)].map((_, index) => {
                const ratingValue = index + 1;
                return (
                    <label key={ratingValue}>
                        <input
                            type="radio"
                            name="rating"
                            value={ratingValue}
                            onClick={() => !disabled && onRatingChange(ratingValue)}
                            className="hidden"
                            disabled={disabled}
                        />
                        <StarIcon
                            className={`cursor-pointer w-8 h-8 ${ratingValue <= (hover || rating) ? 'text-yellow-400' : 'text-gray-300'}`}
                            onMouseEnter={() => !disabled && setHover(ratingValue)}
                            onMouseLeave={() => !disabled && setHover(0)}
                        />
                    </label>
                );
            })}
        </div>
    );
};

export default StarRating;
