import { useState, useEffect } from "react";
import { useParams, useSearchParams } from "react-router-dom";
import { Header } from "@/components/Header";
import { ProductGrid } from "@/components/ProductGrid";
import { SearchBar } from "@/components/SearchBar";
import { PriceFilter } from "@/components/PriceFilter";
import { SortSelect } from "@/components/SortSelect";
import { Pagination } from "@/components/Pagination";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useStore } from "@/hooks/useStore";
import { Filter, X, Grid, List, ArrowLeft } from "lucide-react";
import { ProductFilterParams } from "@/types/product";

const Category = () => {
  const { categoryId } = useParams<{ categoryId: string }>();
  const [searchParams, setSearchParams] = useSearchParams();
  const { products, categories, fetchProducts, fetchCategories } = useStore();

  // Filter states
  const [searchQuery, setSearchQuery] = useState(
    searchParams.get("search") || ""
  );
  const [priceRange, setPriceRange] = useState<[number, number]>([0, 10000]);
  const [inStockOnly, setInStockOnly] = useState(false);
  const [sortBy, setSortBy] = useState<"name" | "price" | "newest">("name");
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid");
  const [showFilters, setShowFilters] = useState(false);

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 12;

  const currentCategory = categories.find((c) => c.id === categoryId);

  // Load initial data
  useEffect(() => {
    fetchCategories();
    loadProducts();
  }, [categoryId]);

  // Load products when filters change
  useEffect(() => {
    loadProducts();
  }, [searchQuery, priceRange, inStockOnly, sortBy, currentPage]);

  const loadProducts = async () => {
    if (!categoryId) return;

    const filters: ProductFilterParams = {
      name: searchQuery || undefined,
      categoryId: parseInt(categoryId),
      minPrice: priceRange[0] || undefined,
      maxPrice: priceRange[1] || undefined,
      inStock: inStockOnly || undefined,
      page: currentPage - 1,
      size: itemsPerPage,
    };

    await fetchProducts(filters);
  };

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    setCurrentPage(1);
    updateURL({ search: query || null, page: null });
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
    if (inStockOnly && product.stock === 0) return false;
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
        return b.id.localeCompare(a.id);
      default:
        return 0;
    }
  });

  const totalPages = Math.ceil(sortedProducts.length / itemsPerPage);
  const paginatedProducts = sortedProducts.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  if (!currentCategory) {
    return (
      <div className="min-h-screen bg-background">
        <Header />
        <div className="container max-w-screen-xl mx-auto px-4 py-8">
          <div className="text-center">
            <h1 className="text-2xl font-bold mb-4">Category not found</h1>
            <Button onClick={() => window.history.back()}>
              <ArrowLeft className="mr-2 h-4 w-4" />
              Go Back
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />

      <div className="container max-w-screen-xl mx-auto px-4 py-8">
        {/* Category Header */}
        <div className="mb-8">
          <div className="flex items-center gap-4 mb-4">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => window.history.back()}
            >
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back
            </Button>
            <Badge variant="outline" className="text-sm">
              {currentCategory.name}
            </Badge>
          </div>

          <h1 className="text-3xl font-bold mb-2">{currentCategory.name}</h1>
          <p className="text-muted-foreground text-lg">
            {currentCategory.description}
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
                    {sortedProducts.length} products in {currentCategory.name}
                  </span>
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
            <ProductGrid
              products={paginatedProducts}
              viewMode={viewMode}
              showFilters={false}
              showHeader={false}
              showCategories={false}
            />

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

export default Category;
