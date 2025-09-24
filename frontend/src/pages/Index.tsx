import { useState } from "react";
import { Header } from "@/components/Header";
import { HeroSection } from "@/components/HeroSection";
import { ProductGrid } from "@/components/ProductGrid";
import { Cart } from "@/components/Cart";
import { AdminDashboard } from "@/components/AdminDashboard";
import { useStore } from "@/hooks/useStore";
import { Button } from "@/components/ui/button";
import { Search } from "lucide-react";

const Index = () => {
  const { currentUser } = useStore();
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  //Show different views based on user role
  if (currentUser === "admin") {
    return <AdminDashboard />;
  }

  if (currentUser === "cashier") {
    return (
      <div className="min-h-screen bg-background">
        <Header
          onCartClick={() => setIsCartOpen(true)}
          onSearchClick={() => setIsSearchOpen(true)}
        />

        <main>
          <HeroSection />
          <ProductGrid />

          {/* About Section */}
          <section
            className="py-16 bg-gradient-to-br from-teddy-50 to-pink-50"
            id="about"
          >
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
                    Quick and safe delivery to bring your new friend home as
                    soon as possible
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
                    <a
                      href="#products"
                      className="hover:text-primary-foreground"
                    >
                      All Products
                    </a>
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
                    <a href="#" className="hover:text-primary-foreground">
                      New Arrivals
                    </a>
                  </li>
                  <li>
                    <a href="#" className="hover:text-primary-foreground">
                      Sale
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
                &copy; 2024 TeddyLove. All rights reserved. Made with ❤️ for
                teddy bear lovers.
              </p>
            </div>
          </div>
        </footer>
      </div>
    );
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
        <ProductGrid />

        {/* About Section */}
        <section
          className="py-16 bg-gradient-to-br from-teddy-50 to-pink-50"
          id="about"
        >
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
                  <a href="#products" className="hover:text-primary-foreground">
                    All Products
                  </a>
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
                  <a href="#" className="hover:text-primary-foreground">
                    New Arrivals
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-primary-foreground">
                    Sale
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
