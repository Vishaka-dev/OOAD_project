import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import { Header } from "@/components/Header";
import { ProductGrid } from "@/components/ProductGrid";
import { CategoryFilter } from "@/components/CategoryFilter";
import { SearchBar } from "@/components/SearchBar";
import { PriceFilter } from "@/components/PriceFilter";
import { SortSelect } from "@/components/SortSelect";
import { Pagination } from "@/components/Pagination";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useStore } from "@/hooks/useStore";
import { Filter, X, Grid, List } from "lucide-react";
import { ProductFilterParams } from "@/types/product";

const Products = () => {
  const { products, categories, fetchProducts, fetchCategories } = useStore();
  const [searchParams, setSearchParams] = useSearchParams();

  // Add loading and error states
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filter states
  const [searchQuery, setSearchQuery] = useState(
    searchParams.get("search") || ""
  );
  const [selectedCategory, setSelectedCategory] = useState<number | null>(
    searchParams.get("category")
      ? parseInt(searchParams.get("category")!)
      : null
  );
  const [priceRange, setPriceRange] = useState<[number, number]>([0, 1000]);
  const [inStockOnly, setInStockOnly] = useState(false);
  const [sortBy, setSortBy] = useState<"name" | "price" | "newest">("name");
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid");
  const [showFilters, setShowFilters] = useState(false);

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 12;

  // Load initial data
  useEffect(() => {
    console.log("🔄 Products component mounted, loading initial data");
    fetchCategories();
    loadProducts();
  }, []);

  // Load products when filters change
  useEffect(() => {
    loadProducts();
  }, [selectedCategory, priceRange, inStockOnly, sortBy, currentPage]);

  const loadProducts = async () => {
    console.log("🔄 Loading products with current state:", {
      searchQuery,
      selectedCategory,
      priceRange,
      inStockOnly,
      sortBy,
      currentPage,
    });

    try {
      setIsLoading(true);
      setError(null);

      const filters: ProductFilterParams = {
        name: searchQuery || undefined,
        categoryId: selectedCategory || undefined,
        minPrice: priceRange[0] || undefined,
        maxPrice: priceRange[1] || undefined,
        inStock: inStockOnly || undefined,
        page: currentPage - 1,
        size: itemsPerPage,
      };

      console.log("🔄 Calling fetchProducts with filters:", filters);
      await fetchProducts(filters);
      console.log("✅ Products loaded successfully");
    } catch (err) {
      console.error("❌ Failed to load products:", err);
      setError(err instanceof Error ? err.message : "Failed to load products");
    } finally {
      setIsLoading(false);
    }
  };

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    setCurrentPage(1);
    updateURL({ search: query || null, page: null });
  };

  const handleCategoryChange = (categoryId: number | null) => {
    setSelectedCategory(categoryId);
    setCurrentPage(1);
    updateURL({ category: categoryId?.toString() || null, page: null });
  };

  const handlePriceRangeChange = (range: [number, number]) => {
    setPriceRange(range);
    setCurrentPage(1);
  };

  const handleSortChange = (sort: "name" | "price" | "newest") => {
    setSortBy(sort);
    setCurrentPage(1);
  };

  const clearFilters = () => {
    setSearchQuery("");
    setSelectedCategory(null);
    setPriceRange([0, 1000]);
    setInStockOnly(false);
    setSortBy("name");
    setCurrentPage(1);
    setSearchParams({});
  };

  const updateURL = (params: Record<string, string | null>) => {
    const newParams = new URLSearchParams(searchParams);

    Object.entries(params).forEach(([key, value]) => {
      if (value) {
        newParams.set(key, value);
      } else {
        newParams.delete(key);
      }
    });

    setSearchParams(newParams);
  };

  const filteredProducts = products.filter((product) => {
    if (inStockOnly && product.stock <= 0) return false;
    if (priceRange[0] > 0 && product.price < priceRange[0]) return false;
    if (priceRange[1] < 1000 && product.price > priceRange[1]) return false;
    return true;
  });

  const sortedProducts = [...filteredProducts].sort((a, b) => {
    switch (sortBy) {
      case "price":
        return a.price - b.price;
      case "name":
        return a.name.localeCompare(b.name);
      case "newest":
        return b.id.localeCompare(a.id); // Assuming higher ID = newer
      default:
        return 0;
    }
  });

  const totalPages = Math.ceil(sortedProducts.length / itemsPerPage);
  const paginatedProducts = sortedProducts.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  // Debug logging
  console.log("🔄 Products component render state:", {
    isLoading,
    error,
    productsCount: products.length,
    filteredProductsCount: filteredProducts.length,
    sortedProductsCount: sortedProducts.length,
    paginatedProductsCount: paginatedProducts.length,
    products: products.slice(0, 3), // Show first 3 products for debugging
  });

  return (
    <div className="min-h-screen bg-background">
      <Header />

      <div className="container max-w-screen-xl mx-auto px-4 py-8">
        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold mb-2">Our Products</h1>
          <p className="text-muted-foreground">
            Discover our collection of premium teddy bears and plush toys
          </p>
        </div>

        <div className="flex flex-col lg:flex-row gap-8">
          {/* Sidebar Filters */}
          <div className="lg:w-64 space-y-6">
            <Card>
              <CardHeader className="pb-4">
                <div className="flex items-center justify-between">
                  <CardTitle className="text-lg">Filters</CardTitle>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={clearFilters}
                    className="text-xs"
                  >
                    Clear All
                  </Button>
                </div>
              </CardHeader>
              <CardContent className="space-y-6">
                <SearchBar
                  value={searchQuery}
                  onChange={handleSearch}
                  placeholder="Search products..."
                />

                <CategoryFilter
                  categories={categories}
                  selectedCategory={selectedCategory}
                  onCategoryChange={handleCategoryChange}
                />

                <PriceFilter
                  priceRange={priceRange}
                  onPriceRangeChange={handlePriceRangeChange}
                />

                <div className="space-y-2">
                  <label className="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      checked={inStockOnly}
                      onChange={(e) => setInStockOnly(e.target.checked)}
                      className="rounded"
                    />
                    <span className="text-sm">In Stock Only</span>
                  </label>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Main Content */}
          <div className="flex-1">
            {/* Toolbar */}
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
              <div className="flex items-center gap-4">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setShowFilters(!showFilters)}
                  className="lg:hidden"
                >
                  <Filter className="h-4 w-4 mr-2" />
                  Filters
                </Button>

                <div className="flex items-center gap-2">
                  <span className="text-sm text-muted-foreground">
                    {sortedProducts.length} products
                  </span>
                  {selectedCategory && (
                    <Badge variant="secondary" className="gap-1">
                      {
                        categories.find(
                          (c) => c.id === selectedCategory.toString()
                        )?.name
                      }
                      <button
                        onClick={() => handleCategoryChange(null)}
                        className="ml-1 hover:bg-destructive/20 rounded-full p-0.5"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </Badge>
                  )}
                </div>
              </div>

              <div className="flex items-center gap-4">
                <SortSelect value={sortBy} onChange={handleSortChange} />

                <div className="flex border rounded-md">
                  <Button
                    variant={viewMode === "grid" ? "default" : "ghost"}
                    size="sm"
                    onClick={() => setViewMode("grid")}
                    className="rounded-r-none"
                  >
                    <Grid className="h-4 w-4" />
                  </Button>
                  <Button
                    variant={viewMode === "list" ? "default" : "ghost"}
                    size="sm"
                    onClick={() => setViewMode("list")}
                    className="rounded-l-none"
                  >
                    <List className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </div>

            {/* Mobile Filters */}
            {showFilters && (
              <Card className="mb-6 lg:hidden">
                <CardContent className="p-4 space-y-4">
                  <SearchBar
                    value={searchQuery}
                    onChange={handleSearch}
                    placeholder="Search products..."
                  />

                  <CategoryFilter
                    categories={categories}
                    selectedCategory={selectedCategory}
                    onCategoryChange={handleCategoryChange}
                  />

                  <PriceFilter
                    priceRange={priceRange}
                    onPriceRangeChange={handlePriceRangeChange}
                  />

                  <div className="space-y-2">
                    <label className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        checked={inStockOnly}
                        onChange={(e) => setInStockOnly(e.target.checked)}
                        className="rounded"
                      />
                      <span className="text-sm">In Stock Only</span>
                    </label>
                  </div>
                </CardContent>
              </Card>
            )}

            {/* Products Grid/List */}
            {isLoading ? (
              <div className="flex items-center justify-center py-16">
                <div className="text-center">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
                  <p className="text-muted-foreground">Loading products...</p>
                </div>
              </div>
            ) : error ? (
              <div className="text-center py-16">
                <div className="text-6xl mb-4">😞</div>
                <h3 className="text-xl font-semibold mb-2">
                  Error Loading Products
                </h3>
                <p className="text-muted-foreground mb-4">{error}</p>
                <Button onClick={() => loadProducts()}>Try Again</Button>
              </div>
            ) : (
              <ProductGrid products={paginatedProducts} viewMode={viewMode} />
            )}

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="mt-8">
                <Pagination
                  currentPage={currentPage}
                  totalPages={totalPages}
                  onPageChange={setCurrentPage}
                />
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Products;
