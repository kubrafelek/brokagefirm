import React, { useState } from 'react';
import { Container, Card, Form, Button, Alert } from 'react-bootstrap';
import { toast } from 'react-toastify';
import ApiService from '../services/ApiService';

const Login = ({ onLogin }) => {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const userData = await ApiService.login(formData.username, formData.password);
      toast.success('Login successful!');
      onLogin(userData);
    } catch (error) {
      setError(error.message || 'Login failed');
      toast.error(error.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <Container>
        <Card className="login-card mx-auto">
          <Card.Header className="text-center">
            <h3 className="mb-0">Brokage Firm</h3>
            <small className="text-muted">Trading Platform</small>
          </Card.Header>
          <Card.Body className="p-4">
            {error && <Alert variant="danger">{error}</Alert>}

            <Form onSubmit={handleSubmit}>
              <Form.Group className="mb-3">
                <Form.Label>Username</Form.Label>
                <Form.Control
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  placeholder="Enter username"
                  required
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Password</Form.Label>
                <Form.Control
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Enter password"
                  required
                />
              </Form.Group>

              <Button
                variant="primary"
                type="submit"
                className="w-100"
                disabled={loading}
              >
                {loading ? 'Logging in...' : 'Login'}
              </Button>
            </Form>
          </Card.Body>
        </Card>
      </Container>
    </div>
  );
};

export default Login;
