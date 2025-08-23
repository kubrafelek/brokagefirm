import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Table, Button, Alert } from 'react-bootstrap';
import { toast } from 'react-toastify';
import ApiService from '../services/ApiService';
import LoadingSpinner from './LoadingSpinner';

const Dashboard = ({ user }) => {
  const [assets, setAssets] = useState([]);
  const [recentOrders, setRecentOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [assetsData, ordersData] = await Promise.all([
        ApiService.getAssets(),
        ApiService.getOrders()
      ]);

      setAssets(assetsData);
      setRecentOrders(ordersData.slice(0, 5)); // Show only recent 5 orders
      setError('');
    } catch (error) {
      setError('Failed to load dashboard data');
      toast.error('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const calculateTotalValue = () => {
    return assets.reduce((total, asset) => {
      if (asset.assetName === 'TRY') {
        return total + parseFloat(asset.size || 0);
      }
      // For other assets, we'd need market prices to calculate value
      // For now, just show count
      return total;
    }, 0);
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'PENDING': return 'bg-warning text-dark';
      case 'MATCHED': return 'bg-success';
      case 'CANCELLED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <Container className="mt-4">
      <Row>
        <Col>
          <h2>Dashboard</h2>
          <p className="text-muted">Welcome back, {user.username}!</p>
        </Col>
      </Row>

      {error && (
        <Row>
          <Col>
            <Alert variant="danger">{error}</Alert>
          </Col>
        </Row>
      )}

      {/* Portfolio Summary */}
      <Row className="mb-4">
        <Col md={4}>
          <Card className="dashboard-stats text-white">
            <Card.Body>
              <Card.Title>Total Cash (TRY)</Card.Title>
              <h3>₺{calculateTotalValue().toLocaleString()}</h3>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="bg-success text-white">
            <Card.Body>
              <Card.Title>Total Assets</Card.Title>
              <h3>{assets.length}</h3>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="bg-info text-white">
            <Card.Body>
              <Card.Title>Recent Orders</Card.Title>
              <h3>{recentOrders.length}</h3>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Row>
        {/* Assets Summary */}
        <Col md={6}>
          <Card>
            <Card.Header>
              <h5 className="mb-0">My Assets</h5>
            </Card.Header>
            <Card.Body>
              {assets.length === 0 ? (
                <p className="text-muted">No assets found</p>
              ) : (
                <Table responsive>
                  <thead>
                    <tr>
                      <th>Asset</th>
                      <th>Size</th>
                      <th>Usable</th>
                    </tr>
                  </thead>
                  <tbody>
                    {assets.map((asset, index) => (
                      <tr key={index}>
                        <td>
                          <strong>{asset.assetName}</strong>
                        </td>
                        <td>{parseFloat(asset.size).toLocaleString()}</td>
                        <td>{parseFloat(asset.usableSize).toLocaleString()}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Col>

        {/* Recent Orders */}
        <Col md={6}>
          <Card>
            <Card.Header>
              <h5 className="mb-0">Recent Orders</h5>
            </Card.Header>
            <Card.Body>
              {recentOrders.length === 0 ? (
                <p className="text-muted">No recent orders</p>
              ) : (
                <Table responsive>
                  <thead>
                    <tr>
                      <th>Asset</th>
                      <th>Side</th>
                      <th>Size</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {recentOrders.map((order) => (
                      <tr key={order.id}>
                        <td><strong>{order.assetName}</strong></td>
                        <td>
                          <span className={`badge ${order.orderSide === 'BUY' ? 'bg-success' : 'bg-danger'}`}>
                            {order.orderSide}
                          </span>
                        </td>
                        <td>{parseFloat(order.size).toLocaleString()}</td>
                        <td>
                          <span className={`badge ${getStatusBadgeClass(order.status)}`}>
                            {order.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Row className="mt-4">
        <Col>
          <Card>
            <Card.Body className="text-center">
              <h5>Quick Actions</h5>
              <div className="d-flex justify-content-center gap-3 mt-3">
                <Button variant="primary" href="/orders">
                  View All Orders
                </Button>
                <Button variant="success" href="/assets">
                  Manage Assets
                </Button>
                {user.isAdmin && (
                  <Button variant="warning" href="/admin">
                    Admin Panel
                  </Button>
                )}
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Dashboard;
