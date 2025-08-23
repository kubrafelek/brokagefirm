import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Table, Alert, Badge } from 'react-bootstrap';
import { toast } from 'react-toastify';
import ApiService from '../services/ApiService';
import LoadingSpinner from './LoadingSpinner';

const Assets = ({ user }) => {
  const [assets, setAssets] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAssets();
  }, []);

  const fetchAssets = async () => {
    try {
      setLoading(true);
      const assetsData = await ApiService.getAssets();
      setAssets(assetsData);
    } catch (error) {
      toast.error('Failed to load assets');
    } finally {
      setLoading(false);
    }
  };

  const calculateTotalValue = () => {
    const tryAsset = assets.find(asset => asset.assetName === 'TRY');
    return tryAsset ? parseFloat(tryAsset.size) : 0;
  };

  const getAssetTypeIcon = (assetName) => {
    switch (assetName) {
      case 'TRY': return '💰';
      case 'AAPL': return '🍎';
      case 'GOOGL': return '🔍';
      case 'MSFT': return '🖥️';
      case 'NVDA': return '🎮';
      case 'TSLA': return '🚗';
      default: return '📈';
    }
  };

  const getAssetColor = (assetName) => {
    switch (assetName) {
      case 'TRY': return 'success';
      case 'AAPL': return 'primary';
      case 'GOOGL': return 'warning';
      case 'MSFT': return 'info';
      case 'NVDA': return 'secondary';
      case 'TSLA': return 'danger';
      default: return 'dark';
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <Container className="mt-4">
      <Row>
        <Col>
          <h2>My Assets</h2>
          <p className="text-muted">Overview of your portfolio holdings</p>
        </Col>
      </Row>

      {/* Portfolio Summary */}
      <Row className="mb-4">
        <Col md={4}>
          <Card className="bg-primary text-white">
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
          <Card className="bg-warning text-white">
            <Card.Body>
              <Card.Title>Stock Holdings</Card.Title>
              <h3>{assets.filter(a => a.assetName !== 'TRY').length}</h3>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Assets Grid */}
      <Row className="mb-4">
        {assets.map((asset, index) => (
          <Col md={6} lg={4} key={index} className="mb-3">
            <Card className="asset-card h-100">
              <Card.Body>
                <div className="d-flex align-items-center mb-3">
                  <span className="fs-2 me-3">{getAssetTypeIcon(asset.assetName)}</span>
                  <div>
                    <h5 className="mb-0">{asset.assetName}</h5>
                    <Badge bg={getAssetColor(asset.assetName)}>
                      {asset.assetName === 'TRY' ? 'Cash' : 'Stock'}
                    </Badge>
                  </div>
                </div>
                <Row>
                  <Col>
                    <small className="text-muted">Total Size</small>
                    <div className="fw-bold">
                      {parseFloat(asset.size).toLocaleString()}
                      {asset.assetName === 'TRY' && ' ₺'}
                    </div>
                  </Col>
                  <Col>
                    <small className="text-muted">Usable</small>
                    <div className="fw-bold text-success">
                      {parseFloat(asset.usableSize).toLocaleString()}
                      {asset.assetName === 'TRY' && ' ₺'}
                    </div>
                  </Col>
                </Row>
                {parseFloat(asset.size) !== parseFloat(asset.usableSize) && (
                  <div className="mt-2">
                    <small className="text-warning">
                      ⚠️ {(parseFloat(asset.size) - parseFloat(asset.usableSize)).toLocaleString()} reserved in pending orders
                    </small>
                  </div>
                )}
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>

      {/* Detailed Table */}
      <Row>
        <Col>
          <Card>
            <Card.Header>
              <h5 className="mb-0">Detailed Asset Information</h5>
            </Card.Header>
            <Card.Body>
              {assets.length === 0 ? (
                <Alert variant="info">
                  No assets found. Start trading to build your portfolio!
                </Alert>
              ) : (
                <Table responsive hover>
                  <thead>
                    <tr>
                      <th>Asset</th>
                      <th>Type</th>
                      <th>Total Size</th>
                      <th>Usable Size</th>
                      <th>Reserved</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {assets.map((asset, index) => {
                      const reserved = parseFloat(asset.size) - parseFloat(asset.usableSize);
                      return (
                        <tr key={index}>
                          <td>
                            <div className="d-flex align-items-center">
                              <span className="me-2">{getAssetTypeIcon(asset.assetName)}</span>
                              <strong>{asset.assetName}</strong>
                            </div>
                          </td>
                          <td>
                            <Badge bg={getAssetColor(asset.assetName)}>
                              {asset.assetName === 'TRY' ? 'Cash' : 'Stock'}
                            </Badge>
                          </td>
                          <td>
                            {parseFloat(asset.size).toLocaleString()}
                            {asset.assetName === 'TRY' && ' ₺'}
                          </td>
                          <td className="text-success">
                            {parseFloat(asset.usableSize).toLocaleString()}
                            {asset.assetName === 'TRY' && ' ₺'}
                          </td>
                          <td className={reserved > 0 ? 'text-warning' : 'text-muted'}>
                            {reserved.toLocaleString()}
                            {asset.assetName === 'TRY' && ' ₺'}
                          </td>
                          <td>
                            {reserved > 0 ? (
                              <Badge bg="warning" text="dark">Partially Reserved</Badge>
                            ) : (
                              <Badge bg="success">Available</Badge>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Information Cards */}
      <Row className="mt-4">
        <Col md={6}>
          <Card>
            <Card.Header>
              <h6 className="mb-0">💡 Trading Tips</h6>
            </Card.Header>
            <Card.Body>
              <ul className="mb-0">
                <li>Usable balance shows what you can trade with</li>
                <li>Reserved amounts are locked in pending orders</li>
                <li>Cancelled orders release reserved funds immediately</li>
                <li>Matched orders transfer assets permanently</li>
              </ul>
            </Card.Body>
          </Card>
        </Col>
        <Col md={6}>
          <Card>
            <Card.Header>
              <h6 className="mb-0">📊 Portfolio Summary</h6>
            </Card.Header>
            <Card.Body>
              <div className="mb-2">
                <small className="text-muted">Cash Position:</small>
                <div className="fw-bold">
                  ₺{(assets.find(a => a.assetName === 'TRY')?.usableSize || 0).toLocaleString()}
                </div>
              </div>
              <div>
                <small className="text-muted">Stock Holdings:</small>
                <div className="fw-bold">
                  {assets.filter(a => a.assetName !== 'TRY').length} different stocks
                </div>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Assets;
