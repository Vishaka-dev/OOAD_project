import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Header } from "@/components/Header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { useStore } from "@/hooks/useStore";
import { useToast } from "@/hooks/use-toast";
import {
  User,
  Mail,
  ShoppingBag,
  Package,
  Clock,
  CheckCircle,
  Truck,
  XCircle,
  ArrowLeft,
  ChevronRight,
} from "lucide-react";
import { apiClient } from "@/lib/api";

interface OrderItem {
  itemId: number;
  productName: string;
  quantity: number;
  price: number;
  itemTotal: number;
  personalizationDetails?: any;
}

interface Order {
  orderId: number;
  orderDate: string;
  status: string;
  totalPrice: number;
  deliveryAddress: string;
  contactNumber: string;
  orderItems: OrderItem[];
}

const Profile = () => {
  const { currentUser, userInfo, logout } = useStore();
  const { toast } = useToast();
  const navigate = useNavigate();

  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [expandedOrderId, setExpandedOrderId] = useState<number | null>(null);

  useEffect(() => {
    // Redirect if not logged in
    if (!currentUser || currentUser !== "customer") {
      navigate("/auth");
      return;
    }

    loadOrders();
  }, [currentUser, navigate]);

  const loadOrders = async () => {
    try {
      setIsLoading(true);
      const token = localStorage.getItem("auth_token");

      const response = await fetch(
        "http://localhost:8081/api/orders/my-orders",
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error("Failed to fetch orders");
      }

      const data = await response.json();
      setOrders(data);
    } catch (error) {
      console.error("Failed to load orders:", error);
      toast({
        title: "Error",
        description: "Failed to load order history.",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case "pending":
        return <Clock className="h-4 w-4" />;
      case "confirmed":
        return <CheckCircle className="h-4 w-4" />;
      case "shipped":
        return <Truck className="h-4 w-4" />;
      case "delivered":
        return <Package className="h-4 w-4" />;
      case "cancelled":
        return <XCircle className="h-4 w-4" />;
      default:
        return <Clock className="h-4 w-4" />;
    }
  };

  const getStatusVariant = (
    status: string
  ): "default" | "secondary" | "destructive" | "outline" => {
    switch (status.toLowerCase()) {
      case "delivered":
        return "default";
      case "shipped":
        return "secondary";
      case "confirmed":
        return "outline";
      case "cancelled":
        return "destructive";
      default:
        return "secondary";
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <div className="min-h-screen bg-background">
      <Header />

      <div className="container max-w-6xl mx-auto px-4 py-8">
        {/* Back Button */}
        <Button variant="ghost" onClick={() => navigate("/")} className="mb-6">
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Home
        </Button>

        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold mb-2">My Account</h1>
          <p className="text-muted-foreground">
            Manage your account and view your order history
          </p>
        </div>

        <div className="grid gap-6 md:grid-cols-3">
          {/* Account Information Card */}
          <div className="md:col-span-1">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <User className="h-5 w-5" />
                  Account Information
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <p className="text-sm text-muted-foreground">Username</p>
                  <p className="font-medium">{userInfo?.username || "N/A"}</p>
                </div>
                <Separator />
                <div>
                  <p className="text-sm text-muted-foreground">Email</p>
                  <p className="font-medium flex items-center gap-2">
                    <Mail className="h-4 w-4" />
                    {userInfo?.email || "N/A"}
                  </p>
                </div>
                <Separator />
                <div>
                  <p className="text-sm text-muted-foreground">Account Type</p>
                  <Badge variant="outline" className="mt-1">
                    {currentUser === "customer" ? "Customer" : currentUser}
                  </Badge>
                </div>
                <Separator />
                <Button
                  variant="outline"
                  onClick={handleLogout}
                  className="w-full"
                >
                  Sign Out
                </Button>
              </CardContent>
            </Card>

            {/* Order Summary Card */}
            <Card className="mt-6">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <ShoppingBag className="h-5 w-5" />
                  Order Summary
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">
                      Total Orders
                    </span>
                    <span className="font-semibold">{orders.length}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">
                      Pending
                    </span>
                    <span className="font-semibold">
                      {
                        orders.filter(
                          (o) => o.status.toLowerCase() === "pending"
                        ).length
                      }
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">
                      Delivered
                    </span>
                    <span className="font-semibold">
                      {
                        orders.filter(
                          (o) => o.status.toLowerCase() === "delivered"
                        ).length
                      }
                    </span>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Order History */}
          <div className="md:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Package className="h-5 w-5" />
                  Order History
                </CardTitle>
              </CardHeader>
              <CardContent>
                {isLoading ? (
                  <div className="space-y-4">
                    {[1, 2, 3].map((i) => (
                      <div key={i} className="animate-pulse">
                        <div className="h-24 bg-muted rounded"></div>
                      </div>
                    ))}
                  </div>
                ) : orders.length === 0 ? (
                  <div className="text-center py-12">
                    <Package className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                    <h3 className="text-lg font-semibold mb-2">
                      No orders yet
                    </h3>
                    <p className="text-muted-foreground mb-4">
                      Start shopping to see your orders here
                    </p>
                    <Button asChild>
                      <Link to="/products">Browse Products</Link>
                    </Button>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {orders.map((order) => (
                      <div
                        key={order.orderId}
                        className="border rounded-lg overflow-hidden hover:border-primary/50 transition-colors"
                      >
                        {/* Order Header */}
                        <div
                          className="p-4 bg-muted/50 cursor-pointer"
                          onClick={() =>
                            setExpandedOrderId(
                              expandedOrderId === order.orderId
                                ? null
                                : order.orderId
                            )
                          }
                        >
                          <div className="flex items-start justify-between">
                            <div className="space-y-1">
                              <div className="flex items-center gap-2">
                                <span className="font-semibold">
                                  Order #{order.orderId}
                                </span>
                                <Badge variant={getStatusVariant(order.status)}>
                                  <span className="flex items-center gap-1">
                                    {getStatusIcon(order.status)}
                                    {order.status}
                                  </span>
                                </Badge>
                              </div>
                              <p className="text-sm text-muted-foreground">
                                Placed on{" "}
                                {new Date(order.orderDate).toLocaleDateString(
                                  "en-US",
                                  {
                                    year: "numeric",
                                    month: "long",
                                    day: "numeric",
                                  }
                                )}
                              </p>
                              <p className="text-sm text-muted-foreground">
                                {order.orderItems?.length || 0} item(s)
                              </p>
                            </div>
                            <div className="text-right">
                              <p className="text-lg font-bold">
                                Rs {order.totalPrice.toFixed(2)}
                              </p>
                              <ChevronRight
                                className={`h-5 w-5 text-muted-foreground transition-transform ${
                                  expandedOrderId === order.orderId
                                    ? "rotate-90"
                                    : ""
                                }`}
                              />
                            </div>
                          </div>
                        </div>

                        {/* Order Details (Expandable) */}
                        {expandedOrderId === order.orderId && (
                          <div className="p-4 space-y-4">
                            {/* Order Items */}
                            <div>
                              <h4 className="font-semibold mb-3">
                                Order Items
                              </h4>
                              <div className="space-y-2">
                                {order.orderItems?.map((item) => (
                                  <div
                                    key={item.itemId}
                                    className="flex justify-between items-center p-3 bg-muted/30 rounded"
                                  >
                                    <div className="flex-1">
                                      <p className="font-medium">
                                        {item.productName}
                                      </p>
                                      <p className="text-sm text-muted-foreground">
                                        Quantity: {item.quantity} × Rs
                                        {item.price.toFixed(2)}
                                      </p>
                                      {item.personalizationDetails && (
                                        <Badge
                                          variant="outline"
                                          className="mt-1"
                                        >
                                          Personalized
                                        </Badge>
                                      )}
                                    </div>
                                    <p className="font-semibold">
                                      Rs {item.itemTotal.toFixed(2)}
                                    </p>
                                  </div>
                                ))}
                              </div>
                            </div>

                            <Separator />

                            {/* Delivery Information */}
                            <div className="grid md:grid-cols-2 gap-4">
                              <div>
                                <h4 className="font-semibold mb-2">
                                  Delivery Address
                                </h4>
                                <p className="text-sm text-muted-foreground">
                                  {order.deliveryAddress}
                                </p>
                              </div>
                              <div>
                                <h4 className="font-semibold mb-2">
                                  Contact Number
                                </h4>
                                <p className="text-sm text-muted-foreground">
                                  {order.contactNumber}
                                </p>
                              </div>
                            </div>

                            <Separator />

                            {/* Order Total */}
                            <div className="flex justify-between items-center pt-2">
                              <span className="font-semibold">
                                Total Amount
                              </span>
                              <span className="text-xl font-bold">
                                Rs {order.totalPrice.toFixed(2)}
                              </span>
                            </div>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
