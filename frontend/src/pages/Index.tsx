import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { Header } from "@/components/Header";
import { HeroSection } from "@/components/HeroSection";
import { ProductGrid } from "@/components/ProductGrid";
import { Cart } from "@/components/Cart";
import { AdminDashboard } from "@/components/AdminDashboard";
import { POSSystem } from "@/components/POSSystem";
import { useStore } from "@/hooks/useStore";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Search, ArrowRight, Star } from "lucide-react";

const Index = () => {
  const { currentUser, products, categories, fetchProducts, fetchCategories } =
    useStore();
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  // Load initial data
  useEffect(() => {
    const loadData = async () => {
      try {
        await Promise.all([
          fetchProducts({ page: 0, size: 8 }), // Load first 8 products
          fetchCategories(),
        ]);
      } catch (error) {
        console.error("Failed to load homepage data:", error);
      } finally {
        setIsLoading(false);
      }
    };
    loadData();
  }, [fetchProducts, fetchCategories]);

  // Get featured products (first 6 products)
  const featuredProducts = products.slice(0, 6);
  const popularCategories = categories.slice(0, 6);

  //Show different views based on user role
  if (currentUser === "admin") {
    return <AdminDashboard />;
  }

  if (currentUser === "cashier") {
    return <POSSystem />;
  }

  //Default customer view
  return (
    <div className="min-h-screen bg-background">
      <Header
        onCartClick={() => setIsCartOpen(true)}
        onSearchClick={() => setIsSearchOpen(true)}
      />

      <main>
        <HeroSection />

        {/* Featured Products Section */}
        <section className="py-16 bg-background" id="featured">
          <div className="container max-w-screen-xl">
            <div className="text-center mb-12">
              <h2 className="text-3xl md:text-4xl font-bold mb-4">
                <span className="bg-gradient-to-r from-teddy-700 to-pink-600 bg-clip-text text-transparent">
                  Featured Products
                </span>
              </h2>
              <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
                Discover our handpicked selection of premium teddy bears and
                plush toys
              </p>
            </div>

            {isLoading ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {[...Array(6)].map((_, i) => (
                  <Card key={i} className="animate-pulse">
                    <div className="aspect-square bg-muted rounded-t-lg"></div>
                    <CardContent className="p-4 space-y-2">
                      <div className="h-4 bg-muted rounded w-3/4"></div>
                      <div className="h-3 bg-muted rounded w-1/2"></div>
                      <div className="h-6 bg-muted rounded w-1/4"></div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {featuredProducts.map((product) => (
                  <Card
                    key={product.id}
                    className="group cursor-pointer overflow-hidden border-border transition-all duration-300 hover:shadow-lg hover:shadow-teddy-300/20 hover:-translate-y-1"
                  >
                    <div className="relative aspect-square overflow-hidden bg-gradient-to-br from-teddy-50 to-pink-50">
                      <img
                        src={product.image}
                        alt={product.name}
                        className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                      />
                      {product.featured && (
                        <Badge className="absolute top-2 left-2 bg-pink-500 text-primary-foreground">
                          Featured
                        </Badge>
                      )}
                      {product.stock <= 5 && product.stock > 0 && (
                        <Badge className="absolute bottom-2 left-2 bg-destructive text-destructive-foreground">
                          Only {product.stock} left
                        </Badge>
                      )}
                    </div>
                    <CardContent className="p-4">
                      <div className="space-y-2">
                        <h3 className="font-semibold text-foreground group-hover:text-primary transition-colors">
                          {product.name}
                        </h3>
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {product.description}
                        </p>

                        <div className="flex items-center space-x-1">
                          {[...Array(5)].map((_, i) => (
                            <Star
                              key={i}
                              className={`h-3 w-3 ${
                                i < Math.floor(product.rating)
                                  ? "fill-yellow-400 text-yellow-400"
                                  : "fill-muted text-muted"
                              }`}
                            />
                          ))}
                          <span className="text-xs text-muted-foreground ml-1">
                            ({product.reviews})
                          </span>
                        </div>

                        <div className="flex items-center justify-between">
                          <span className="text-lg font-bold text-foreground">
                            Rs {product.price.toFixed(2)}
                          </span>
                          <Badge variant="outline" className="text-xs">
                            {product.category}
                          </Badge>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}

            <div className="text-center mt-8">
              <Button asChild size="lg">
                <Link to="/products">
                  View All Products
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
            </div>
          </div>
        </section>

        {/* Categories Section */}
        <section
          className="py-16 bg-gradient-to-br from-teddy-50 to-pink-50"
          id="categories"
        >
          <div className="container max-w-screen-xl">
            <div className="text-center mb-12">
              <h2 className="text-3xl md:text-4xl font-bold mb-4">
                <span className="bg-gradient-to-r from-teddy-700 to-pink-600 bg-clip-text text-transparent">
                  Shop by Category
                </span>
              </h2>
              <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
                Find the perfect teddy bear for every occasion and personality
              </p>
            </div>

            {isLoading ? (
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
                {[...Array(6)].map((_, i) => (
                  <Card key={i} className="animate-pulse">
                    <CardContent className="p-6 text-center space-y-2">
                      <div className="text-4xl mb-2">🧸</div>
                      <div className="h-4 bg-muted rounded w-3/4 mx-auto"></div>
                      <div className="h-3 bg-muted rounded w-1/2 mx-auto"></div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            ) : (
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
                {popularCategories.map((category) => (
                  <Card
                    key={category.id}
                    className="group cursor-pointer hover:shadow-lg transition-all duration-300"
                  >
                    <CardContent className="p-6 text-center space-y-2">
                      <div className="text-4xl mb-2 group-hover:scale-110 transition-transform">
                        {category.name === "Classic"
                          ? "🧸"
                          : category.name === "Princess"
                          ? "👑"
                          : category.name === "Mini"
                          ? "🤏"
                          : category.name === "Giant"
                          ? "🦣"
                          : category.name === "Scented"
                          ? "🌸"
                          : category.name === "Adventure"
                          ? "🎒"
                          : "🧸"}
                      </div>
                      <h3 className="font-semibold group-hover:text-primary transition-colors">
                        {category.name}
                      </h3>
                      <p className="text-xs text-muted-foreground">
                        {category.productCount} products
                      </p>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}

            <div className="text-center mt-8">
              <Button asChild variant="outline" size="lg">
                <Link to="/products">
                  Explore All Categories
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
            </div>
          </div>
        </section>

        {/* About Section */}
        <section className="py-16 bg-background" id="about">
          <div className="container max-w-screen-xl text-center">
            <h2 className="text-3xl md:text-4xl font-bold mb-8">
              <span className="bg-gradient-to-r from-teddy-700 to-pink-600 bg-clip-text text-transparent">
                Why Choose TeddyLove?
              </span>
            </h2>
            <div className="grid md:grid-cols-3 gap-8">
              <div className="space-y-4">
                <div className="text-4xl">🧸</div>
                <h3 className="text-xl font-semibold">Premium Quality</h3>
                <p className="text-muted-foreground">
                  Each teddy bear is crafted with the finest materials and
                  attention to detail
                </p>
              </div>
              <div className="space-y-4">
                <div className="text-4xl">❤️</div>
                <h3 className="text-xl font-semibold">Made with Love</h3>
                <p className="text-muted-foreground">
                  Every bear is designed to bring joy and comfort to people of
                  all ages
                </p>
              </div>
              <div className="space-y-4">
                <div className="text-4xl">🚚</div>
                <h3 className="text-xl font-semibold">Fast Delivery</h3>
                <p className="text-muted-foreground">
                  Quick and safe delivery to bring your new friend home as soon
                  as possible
                </p>
              </div>
            </div>
          </div>
        </section>
      </main>

      <Cart isOpen={isCartOpen} onClose={() => setIsCartOpen(false)} />

      {/* Footer */}
      <footer className="bg-teddy-800 text-primary-foreground py-12">
        <div className="container max-w-screen-xl">
          <div className="grid md:grid-cols-4 gap-8">
            <div>
              <div className="flex items-center space-x-2 mb-4">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-teddy-600 to-pink-500">
                  <span className="text-lg font-bold">🧸</span>
                </div>
                <h3 className="text-xl font-bold">TeddyLove</h3>
              </div>
              <p className="text-teddy-200">
                Bringing joy and comfort through premium teddy bears and plush
                toys.
              </p>
            </div>
            <div>
              <h4 className="font-semibold mb-4">Shop</h4>
              <ul className="space-y-2 text-teddy-200">
                <li>
                  <Link
                    to="/products"
                    className="hover:text-primary-foreground"
                  >
                    All Products
                  </Link>
                </li>
                <li>
                  <a
                    href="#categories"
                    className="hover:text-primary-foreground"
                  >
                    Categories
                  </a>
                </li>
                <li>
                  <a href="#featured" className="hover:text-primary-foreground">
                    Featured
                  </a>
                </li>
                <li>
                  <a href="#about" className="hover:text-primary-foreground">
                    About Us
                  </a>
                </li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4">Support</h4>
              <ul className="space-y-2 text-teddy-200">
                <li>
                  <a href="#" className="hover:text-primary-foreground">
                    Contact Us
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-primary-foreground">
                    Shipping Info
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-primary-foreground">
                    Returns
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-primary-foreground">
                    FAQ
                  </a>
                </li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4">Connect</h4>
              <ul className="space-y-2 text-teddy-200">
                <li>
                  <a href="#" className="hover:text-primary-foreground">
                    Facebook
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-primary-foreground">
                    Instagram
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-primary-foreground">
                    Twitter
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-primary-foreground">
                    Pinterest
                  </a>
                </li>
              </ul>
            </div>
          </div>
          <div className="border-t border-teddy-700 mt-8 pt-8 text-center text-teddy-200">
            <p>
              &copy; 2024 TeddyLove. All rights reserved. Made with ❤️ for teddy
              bear lovers.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Index;
