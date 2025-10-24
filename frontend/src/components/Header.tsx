import { ShoppingCart, User, Heart, Search, Menu, X } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { useStore } from "@/hooks/useStore";
import { Badge } from "@/components/ui/badge";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useState } from "react";

interface HeaderProps {
  onCartClick: () => void;
  onSearchClick: () => void;
}

export function Header({ onCartClick, onSearchClick }: HeaderProps) {
  const { cart, currentUser, setCurrentUser, userInfo, logout } = useStore();
  const cartItemsCount = cart.reduce((total, item) => total + item.quantity, 0);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-16 max-w-screen-2xl items-center justify-between">
        {/* Logo */}
        <div className="flex items-center space-x-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-teddy-600 to-pink-500">
            <span className="text-lg font-bold text-primary-foreground">
              🧸
            </span>
          </div>
          <h1 className="text-xl sm:text-2xl font-bold bg-gradient-to-r from-teddy-700 to-pink-600 bg-clip-text text-transparent">
            Pinky Promise
          </h1>
        </div>

        {/* Navigation */}
        <nav className="hidden md:flex items-center space-x-6">
          <Link
            to="/"
            className="text-sm font-medium transition-colors hover:text-primary"
          >
            Home
          </Link>
          <Link
            to="/products"
            className="text-sm font-medium transition-colors hover:text-primary"
          >
            Products
          </Link>
          <a
            href="#categories"
            className="text-sm font-medium transition-colors hover:text-primary"
          >
            Categories
          </a>
          <a
            href="#about"
            className="text-sm font-medium transition-colors hover:text-primary"
          >
            About
          </a>
        </nav>

        {/* Actions */}
        <div className="flex items-center space-x-1 sm:space-x-2">
          <Button
            variant="ghost"
            size="icon"
            onClick={onSearchClick}
            className="hidden sm:flex"
          >
            <Search className="h-4 w-4" />
          </Button>

          <Button variant="ghost" size="icon" className="hidden sm:flex">
            <Heart className="h-4 w-4" />
          </Button>

          <Button
            variant="ghost"
            size="icon"
            onClick={onCartClick}
            className="relative"
          >
            <ShoppingCart className="h-4 w-4" />
            {cartItemsCount > 0 && (
              <Badge className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs bg-pink-500">
                {cartItemsCount}
              </Badge>
            )}
          </Button>

          {/* User Menu */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon">
                <User className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent
              align="end"
              className="w-56 bg-popover border border-border z-50"
            >
              {currentUser ? (
                <>
                  <div className="px-2 py-1.5 text-sm text-muted-foreground">
                    {userInfo
                      ? `Welcome, ${userInfo.username}`
                      : `Logged in as ${currentUser}`}
                  </div>
                  {currentUser === "customer" && (
                    <DropdownMenuItem asChild>
                      <Link to="/profile">👤 My Profile</Link>
                    </DropdownMenuItem>
                  )}
                  <DropdownMenuItem
                    onClick={() => setCurrentUser("customer")}
                    className={currentUser === "customer" ? "bg-accent" : ""}
                  >
                    👤 Customer View
                  </DropdownMenuItem>
                  {/* <DropdownMenuItem
                    onClick={() => setCurrentUser("admin")}
                    className={currentUser === "admin" ? "bg-accent" : ""}
                  >
                    👨‍💼 Admin Dashboard
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() => setCurrentUser("cashier")}
                    className={currentUser === "cashier" ? "bg-accent" : ""}
                  >
                    🏪 POS System
                  </DropdownMenuItem> */}
                  <DropdownMenuItem onClick={logout}>
                    🚪 Sign Out
                  </DropdownMenuItem>
                </>
              ) : (
                <>
                  <DropdownMenuItem
                    onClick={() => (window.location.href = "/auth")}
                  >
                    🔐 Sign In
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() => setCurrentUser("customer")}
                    className={currentUser === "customer" ? "bg-accent" : ""}
                  >
                    👤 Guest View
                  </DropdownMenuItem>
                </>
              )}
            </DropdownMenuContent>
          </DropdownMenu>

          <Button
            variant="ghost"
            size="icon"
            className="md:hidden"
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          >
            {isMobileMenuOpen ? (
              <X className="h-4 w-4" />
            ) : (
              <Menu className="h-4 w-4" />
            )}
          </Button>
        </div>
      </div>

      {/* Mobile Navigation Menu */}
      {isMobileMenuOpen && (
        <div className="md:hidden border-t bg-background">
          <nav className="container max-w-screen-2xl py-4 flex flex-col space-y-3">
            <Link
              to="/"
              className="px-4 py-2 text-sm font-medium transition-colors hover:text-primary hover:bg-accent rounded-md"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              Home
            </Link>
            <Link
              to="/products"
              className="px-4 py-2 text-sm font-medium transition-colors hover:text-primary hover:bg-accent rounded-md"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              Products
            </Link>
            <a
              href="#categories"
              className="px-4 py-2 text-sm font-medium transition-colors hover:text-primary hover:bg-accent rounded-md"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              Categories
            </a>
            <a
              href="#about"
              className="px-4 py-2 text-sm font-medium transition-colors hover:text-primary hover:bg-accent rounded-md"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              About
            </a>
          </nav>
        </div>
      )}
    </header>
  );
}
