import { useState } from "react";
import { apiClient } from "@/lib/api";

export function ApiTest() {
  const [testResults, setTestResults] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const addResult = (message: string) => {
    setTestResults((prev) => [
      ...prev,
      `${new Date().toLocaleTimeString()}: ${message}`,
    ]);
  };

  const testApiConnection = async () => {
    setIsLoading(true);
    setTestResults([]);

    try {
      addResult("🔄 Testing API connection...");

      // Test 1: Basic connectivity
      addResult("🔄 Testing products endpoint...");
      const productsResponse = await apiClient.getProducts();

      // Handle both direct array and paginated response
      const products = Array.isArray(productsResponse)
        ? productsResponse
        : productsResponse?.content || [];

      addResult(
        `✅ Products API working: ${products.length} products received`
      );

      // Test 2: Categories
      addResult("🔄 Testing categories endpoint...");
      const categories = await apiClient.getCategories();
      addResult(
        `✅ Categories API working: ${categories.length} categories received`
      );

      // Test 3: Show sample data
      if (products.length > 0) {
        const sampleProduct = products[0];
        addResult(
          `📦 Sample product: ${sampleProduct.name} - $${sampleProduct.price} (${sampleProduct.categoryName})`
        );
      }

      if (categories.length > 0) {
        const sampleCategory = categories[0];
        addResult(
          `📂 Sample category: ${sampleCategory.name} (${sampleCategory.productCount} products)`
        );
      }

      addResult("🎉 All API tests passed!");
    } catch (error) {
      addResult(
        `❌ API test failed: ${
          error instanceof Error ? error.message : "Unknown error"
        }`
      );
      console.error("API test error:", error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="p-4 border rounded-lg bg-gray-50">
      <h3 className="text-lg font-semibold mb-4">API Connection Test</h3>

      <div className="flex gap-2 mb-4">
        <button
          onClick={testApiConnection}
          disabled={isLoading}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50"
        >
          {isLoading ? "Testing..." : "Test API Connection"}
        </button>

        <button
          onClick={() => window.location.reload()}
          className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
        >
          Refresh Page
        </button>
      </div>

      <div className="mt-4 max-h-64 overflow-y-auto">
        {testResults.map((result, index) => (
          <div key={index} className="text-sm font-mono mb-1">
            {result}
          </div>
        ))}
      </div>
    </div>
  );
}
