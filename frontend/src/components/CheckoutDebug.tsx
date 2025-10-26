import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useStore } from "@/hooks/useStore";
import { apiClient } from "@/lib/api";

const CheckoutDebug = () => {
  const { cart, currentUser, userInfo } = useStore();
  const [debugInfo, setDebugInfo] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(false);

  const runDebugCheck = async () => {
    setIsLoading(true);
    try {
      const token = localStorage.getItem("auth_token");
      console.log("🔍 Debug Checkout - Starting...");

      const debugData = {
        // User info
        currentUser,
        userInfo,
        hasToken: !!token,
        tokenLength: token?.length || 0,

        // Cart info
        cartLength: cart.length,
        cartItems: cart.map((item) => ({
          id: item.id,
          name: item.name,
          quantity: item.quantity,
          price: item.price,
          stock: item.stock,
        })),

        // API test
        apiTest: null,
      };

      // Test API connectivity
      try {
        const testResponse = await fetch("http://localhost:8081/api/products", {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });
        debugData.apiTest = {
          status: testResponse.status,
          ok: testResponse.ok,
          statusText: testResponse.statusText,
        };
      } catch (apiError) {
        debugData.apiTest = {
          error: apiError.message,
        };
      }

      setDebugInfo(debugData);
      console.log("🔍 Debug Results:", debugData);
    } catch (error) {
      console.error("Debug failed:", error);
      setDebugInfo({ error: error.message });
    } finally {
      setIsLoading(false);
    }
  };

  const testCheckout = async () => {
    try {
      console.log("🧪 Testing checkout...");

      // Check if user is logged in
      if (!currentUser) {
        alert("❌ You must be logged in to test checkout. Please login first.");
        return;
      }

      const token = localStorage.getItem("auth_token");
      if (!token) {
        alert("❌ No authentication token found. Please login first.");
        return;
      }

      const result = await apiClient.checkout({
        deliveryAddress: "Test Address",
        contactNumber: "1234567890",
        paymentMethod: "cod",
      });
      console.log("✅ Checkout test result:", result);
      alert(`Checkout test successful: ${result}`);
    } catch (error) {
      console.error("❌ Checkout test failed:", error);
      alert(`Checkout test failed: ${error.message}`);
    }
  };

  return (
    <Card className="w-full max-w-4xl mx-auto">
      <CardHeader>
        <CardTitle>🔍 Checkout Debug Tool</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex gap-2">
          <Button onClick={runDebugCheck} disabled={isLoading}>
            {isLoading ? "Running..." : "Run Debug Check"}
          </Button>
          <Button onClick={testCheckout} variant="outline">
            Test Checkout API
          </Button>
        </div>

        {debugInfo && (
          <div className="mt-4">
            <h3 className="font-semibold mb-2">Debug Results:</h3>
            <pre className="bg-muted p-4 rounded text-sm overflow-auto max-h-96">
              {JSON.stringify(debugInfo, null, 2)}
            </pre>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default CheckoutDebug;
