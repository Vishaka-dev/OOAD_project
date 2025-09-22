import { useState } from "react";
import {
  Plus,
  Package,
  TrendingUp,
  Users,
  DollarSign,
  Edit,
  Trash2,
  ArrowLeft,
  Home,
  ShoppingCart,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useStore } from "@/hooks/useStore";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

export function AdminDashboard() {
  const { products, orders, updateProduct, setCurrentUser } = useStore();
  const [selectedTab, setSelectedTab] = useState<
    "overview" | "products" | "orders"
  >("overview");

  // Calculate stats
  const totalRevenue = orders.reduce((sum, order) => sum + order.total, 0);
  const totalProducts = products.length;
  const lowStockItems = products.filter((p) => p.stock <= 5 && p.stock > 0);
  const outOfStockItems = products.filter((p) => p.stock === 0);

  const tabs = [
    { id: "overview", label: "Overview", icon: TrendingUp },
    { id: "products", label: "Products", icon: Package },
    { id: "orders", label: "Orders", icon: Users },
  ];

  return (
    <div className="min-h-screen bg-background">
      <div className="container max-w-screen-2xl py-8">
        {/* Navigation Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center space-x-4">
            <Button
              variant="ghost"
              onClick={() => setCurrentUser("customer")}
              className="flex items-center space-x-2"
            >
              <ArrowLeft className="h-4 w-4" />
              <span>Back to Store</span>
            </Button>
            <div className="h-6 w-px bg-border" />
            <div className="flex items-center space-x-2">
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setCurrentUser("customer")}
                className="flex items-center space-x-1"
              >
                <Home className="h-4 w-4" />
                <span>Customer View</span>
              </Button>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setCurrentUser("cashier")}
                className="flex items-center space-x-1"
              >
                <ShoppingCart className="h-4 w-4" />
                <span>POS System</span>
              </Button>
            </div>
          </div>
        </div>

        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold text-foreground">
              Admin Dashboard
            </h1>
            <p className="text-muted-foreground">Manage your TeddyLove store</p>
          </div>
          <Button variant="teddy">
            <Plus className="mr-2 h-4 w-4" />
            Add Product
          </Button>
        </div>

        {/* Tabs */}
        <div className="flex space-x-1 mb-8 bg-muted p-1 rounded-lg w-fit">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            return (
              <Button
                key={tab.id}
                variant={selectedTab === tab.id ? "default" : "ghost"}
                size="sm"
                onClick={() => setSelectedTab(tab.id as any)}
                className="data-[state=active]:bg-background"
              >
                <Icon className="mr-2 h-4 w-4" />
                {tab.label}
              </Button>
            );
          })}
        </div>

        {/* Overview Tab */}
        {selectedTab === "overview" && (
          <div className="space-y-8">
            {/* Stats Cards */}
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">
                    Total Revenue
                  </CardTitle>
                  <DollarSign className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">
                    ${totalRevenue.toFixed(2)}
                  </div>
                  <p className="text-xs text-muted-foreground">
                    +12% from last month
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">
                    Total Products
                  </CardTitle>
                  <Package className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{totalProducts}</div>
                  <p className="text-xs text-muted-foreground">
                    Active inventory
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">
                    Low Stock Alert
                  </CardTitle>
                  <Package className="h-4 w-4 text-destructive" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold text-destructive">
                    {lowStockItems.length}
                  </div>
                  <p className="text-xs text-muted-foreground">
                    Items need restocking
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">
                    Total Orders
                  </CardTitle>
                  <Users className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{orders.length}</div>
                  <p className="text-xs text-muted-foreground">
                    All time orders
                  </p>
                </CardContent>
              </Card>
            </div>

            {/* Low Stock Items */}
            {lowStockItems.length > 0 && (
              <Card>
                <CardHeader>
                  <CardTitle>Low Stock Alert</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {lowStockItems.map((product) => (
                      <div
                        key={product.id}
                        className="flex items-center justify-between p-3 bg-destructive/10 rounded-lg"
                      >
                        <div className="flex items-center space-x-3">
                          <img
                            src={product.image}
                            alt={product.name}
                            className="h-10 w-10 rounded-md object-cover"
                          />
                          <div>
                            <p className="font-medium">{product.name}</p>
                            <p className="text-sm text-muted-foreground">
                              {product.category}
                            </p>
                          </div>
                        </div>
                        <Badge variant="destructive">
                          {product.stock} left
                        </Badge>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            )}
          </div>
        )}

        {/* Products Tab */}
        {selectedTab === "products" && (
          <Card>
            <CardHeader>
              <CardTitle>Product Management</CardTitle>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Product</TableHead>
                    <TableHead>Category</TableHead>
                    <TableHead>Price</TableHead>
                    <TableHead>Stock</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {products.map((product) => (
                    <TableRow key={product.id}>
                      <TableCell className="flex items-center space-x-3">
                        <img
                          src={product.image}
                          alt={product.name}
                          className="h-10 w-10 rounded-md object-cover"
                        />
                        <div>
                          <p className="font-medium">{product.name}</p>
                          <p className="text-sm text-muted-foreground">
                            {product.size}
                          </p>
                        </div>
                      </TableCell>
                      <TableCell>{product.category}</TableCell>
                      <TableCell>${product.price.toFixed(2)}</TableCell>
                      <TableCell>{product.stock}</TableCell>
                      <TableCell>
                        <Badge
                          variant={
                            product.stock === 0
                              ? "destructive"
                              : product.stock <= 5
                              ? "secondary"
                              : "default"
                          }
                        >
                          {product.stock === 0
                            ? "Out of Stock"
                            : product.stock <= 5
                            ? "Low Stock"
                            : "In Stock"}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end space-x-2">
                          <Button variant="ghost" size="sm">
                            <Edit className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            className="text-destructive"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        )}

        {/* Orders Tab */}
        {selectedTab === "orders" && (
          <Card>
            <CardHeader>
              <CardTitle>Order Management</CardTitle>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Order ID</TableHead>
                    <TableHead>Customer</TableHead>
                    <TableHead>Items</TableHead>
                    <TableHead>Total</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Date</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {orders.map((order) => (
                    <TableRow key={order.id}>
                      <TableCell className="font-mono">
                        #{order.id.slice(-8)}
                      </TableCell>
                      <TableCell>{order.customerName || "Guest"}</TableCell>
                      <TableCell>{order.items.length} items</TableCell>
                      <TableCell>${order.total.toFixed(2)}</TableCell>
                      <TableCell>
                        <Badge
                          variant={
                            order.status === "delivered"
                              ? "default"
                              : order.status === "shipped"
                              ? "secondary"
                              : order.status === "processing"
                              ? "outline"
                              : "destructive"
                          }
                        >
                          {order.status.charAt(0).toUpperCase() +
                            order.status.slice(1)}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        {new Date(order.createdAt).toLocaleDateString()}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}
