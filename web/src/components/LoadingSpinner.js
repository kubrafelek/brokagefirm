import React from 'react';
import { Container, Spinner } from 'react-bootstrap';

const LoadingSpinner = () => {
  return (
    <Container className="spinner-container">
      <div className="text-center">
        <Spinner animation="border" role="status" variant="primary">
          <span className="visually-hidden">Loading...</span>
        </Spinner>
        <div className="mt-2">Loading...</div>
      </div>
    </Container>
  );
};

export default LoadingSpinner;
