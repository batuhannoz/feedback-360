import React from 'react';

const LoadingSpinner = () => {
  return (
    <div className="flex justify-center items-center h-full">
      <div className="w-16 h-16 border-4 border-black border-t-transparent rounded-full animate-spin"></div>
    </div>
  );
};

export default LoadingSpinner;
