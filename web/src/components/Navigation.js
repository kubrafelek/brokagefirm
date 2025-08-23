import React from 'react';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';

const Navigation = ({ user, onLogout }) => {
  return (
    <Navbar bg="dark" variant="dark" expand="lg" sticky="top">
      <Container>
        <Navbar.Brand as={Link} to="/dashboard">
          💼 Brokerage Firm
        </Navbar.Brand>

        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/dashboard">Dashboard</Nav.Link>
            <Nav.Link as={Link} to="/orders">Orders</Nav.Link>
            <Nav.Link as={Link} to="/assets">Assets</Nav.Link>
            {user.isAdmin && (
              <Nav.Link as={Link} to="/admin">Admin Panel</Nav.Link>
            )}
          </Nav>

          <Nav>
            <Navbar.Text className="me-3">
              Welcome, {user.username} {user.isAdmin && <span className="badge bg-warning text-dark">Admin</span>}
            </Navbar.Text>
            <Button variant="outline-light" size="sm" onClick={onLogout}>
              Logout
            </Button>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Navigation;
