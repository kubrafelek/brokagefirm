import React from 'react';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';

const CustomerLayout = ({ user, onLogout, children }) => {
  return (
    <div className="customer-layout">
      <Navbar className="customer-navbar" variant="dark" expand="lg" sticky="top">
        <Container>
          <Navbar.Brand as={Link} to="/dashboard">
            💼 Brokerage Firm
          </Navbar.Brand>

          <Navbar.Toggle aria-controls="customer-navbar-nav" />
          <Navbar.Collapse id="customer-navbar-nav">
            <Nav className="me-auto">
              <Nav.Link as={Link} to="/dashboard">Dashboard</Nav.Link>
              <Nav.Link as={Link} to="/orders">My Orders</Nav.Link>
              <Nav.Link as={Link} to="/assets">My Assets</Nav.Link>
              <Nav.Link as={Link} to="/portfolio">Portfolio</Nav.Link>
              <Nav.Link as={Link} to="/profile">Profile</Nav.Link>
            </Nav>

            <Nav>
              <Navbar.Text className="me-3">
                Welcome, {user.username}
                <span className="badge bg-primary ms-2">Customer</span>
              </Navbar.Text>
              <Button variant="outline-light" size="sm" onClick={onLogout}>
                Logout
              </Button>
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>

      <div className="customer-content">
        {children}
      </div>
    </div>
  );
};

export default CustomerLayout;
