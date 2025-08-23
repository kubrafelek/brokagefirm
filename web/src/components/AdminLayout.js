import React from 'react';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';

const AdminLayout = ({ user, onLogout, children }) => {
  return (
    <div className="admin-layout">
      <Navbar className="admin-navbar" variant="dark" expand="lg" sticky="top">
        <Container>
          <Navbar.Brand as={Link} to="/admin">
            🔧 Admin Portal
          </Navbar.Brand>

          <Navbar.Toggle aria-controls="admin-navbar-nav" />
          <Navbar.Collapse id="admin-navbar-nav">
            <Nav>
              <Button variant="outline-light" size="sm" onClick={onLogout}>
                Logout
              </Button>
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>

      <div className="admin-content">
        {children}
      </div>
    </div>
  );
};

export default AdminLayout;
