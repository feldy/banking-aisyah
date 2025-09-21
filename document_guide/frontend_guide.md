### 11.1 Transfer Form
```typescript
// src/components/transactions/TransferForm.tsx
'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useAccounts } from '@/hooks/useAccounts'
import { useTransactions } from '@/hooks/useTransactions'
import { TransferRequest } from '@/types/transaction'
import { formatCurrency } from '@/utils/formatters'
import { ArrowRightIcon } from '@heroicons/react/24/outline'

const transferSchema = z.object({
  fromAccountNumber: z.string().min(1, 'Pilih rekening sumber'),
  toAccountNumber: z.string().min(1, 'Nomor rekening tujuan harus diisi'),
  amount: z.number().min(1, 'Jumlah harus lebih dari 0'),
  description: z.string().min(1, 'Keterangan harus diisi'),
})

type TransferFormData = z.infer<typeof transferSchema>

interface TransferFormProps {
  onSuccess?: () => void
}

export default function TransferForm({ onSuccess }: TransferFormProps) {
  const [isLoading, setIsLoading] = useState(false)
  const [destinationAccountInfo, setDestinationAccountInfo] = useState<any>(null)
  const { accounts } = useAccounts()
  const { transfer } = useTransactions()

  const form = useForm<TransferFormData>({
    resolver: zodResolver(transferSchema),
    defaultValues: {
      fromAccountNumber: '',
      toAccountNumber: '',
      amount: 0,
      description: '',
    },
  })

  const selectedSourceAccount = accounts.find(
    account => account.accountNumber === form.watch('fromAccountNumber')
  )

  const checkDestinationAccount = async (accountNumber: string) => {
    if (accountNumber.length >= 10) {
      try {
        // Mock API call to check account
        // In real app, this would validate the destination account
        setDestinationAccountInfo({
          accountName: 'John Doe',
          bankName: 'Bank Syariah Aisyah'
        })
      } catch (error) {
        setDestinationAccountInfo(null)
      }
    }
  }

  const onSubmit = async (data: TransferFormData) => {
    if (!selectedSourceAccount) return
    
    setIsLoading(true)
    try {
      await transfer({
        fromAccountNumber: data.fromAccountNumber,
        toAccountNumber: data.toAccountNumber,
        amount: data.amount,
        description: data.description,
      })
      
      form.reset()
      onSuccess?.()
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Transfer failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Card className="max-w-2xl mx-auto">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <ArrowRightIcon className="w-5 h-5" />
          Transfer Antar Rekening
        </CardTitle>
        <CardDescription>
          Transfer dana ke rekening lain dengan mudah dan aman
        </CardDescription>
      </CardHeader>
      
      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
            {/* Source Account */}
            <FormField
              control={form.control}
              name="fromAccountNumber"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Rekening Sumber</FormLabel>
                  <Select onValueChange={field.onChange} defaultValue={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Pilih rekening sumber" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {accounts
                        .filter(account => account.status === 'ACTIVE')
                        .map((account) => (
                          <SelectItem key={account.id} value={account.accountNumber}>
                            <div className="flex flex-col">
                              <span className="font-medium">{account.accountName}</span>
                              <span className="text-sm text-gray-500">
                                {account.accountNumber} • {formatCurrency(account.balance)}
                              </span>
                            </div>
                          </SelectItem>
                        ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Destination Account */}
            <FormField
              control={form.control}
              name="toAccountNumber"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Rekening Tujuan</FormLabel>
                  <FormControl>
                    <Input
                      placeholder="Masukkan nomor rekening tujuan"
                      {...field}
                      onChange={(e) => {
                        field.onChange(e)
                        checkDestinationAccount(e.target.value)
                      }}
                    />
                  </FormControl>
                  {destinationAccountInfo && (
                    <div className="mt-2 p-3 bg-green-50 border border-green-200 rounded-md">
                      <p className="text-sm font-medium text-green-800">
                        {destinationAccountInfo.accountName}
                      </p>
                      <p className="text-xs text-green-600">
                        {destinationAccountInfo.bankName}
                      </p>
                    </div>
                  )}
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Amount */}
            <FormField
              control={form.control}
              name="amount"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Jumlah Transfer</FormLabel>
                  <FormControl>
                    <Input
                      type="number"
                      placeholder="0"
                      {...field}
                      onChange={(e) => field.onChange(Number(e.target.value))}
                    />
                  </FormControl>
                  {selectedSourceAccount && field.value > 0 && (
                    <div className="text-sm text-gray-500">
                      Saldo tersedia: {formatCurrency(selectedSourceAccount.availableBalance)}
                    </div>
                  )}
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Description */}
            <FormField
              control={form.control}
              name="description"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Keterangan</FormLabel>
                  <FormControl>
                    <Input
                      placeholder="Masukkan keterangan transfer"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            {form.formState.errors.root && (
              <div className="text-sm text-red-600">
                {form.formState.errors.root.message}
              </div>
            )}

            <Button
              type="submit"
              className="w-full"
              variant="islamic"
              disabled={isLoading || !selectedSourceAccount}
            >
              {isLoading ? 'Memproses Transfer...' : 'Transfer Sekarang'}
            </Button>
          </form>
        </Form>
      </CardContent>
    </Card>
  )
}
```

### 11.2 Transaction History
```typescript
// src/components/transactions/TransactionHistory.tsx
'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Badge } from '@/components/ui/badge'
import { Transaction } from '@/types/transaction'
import { formatCurrency, formatDate } from '@/utils/formatters'
import { useAccounts } from '@/hooks/useAccounts'
import { apiService } from '@/lib/api'
import { 
  ArrowUpIcon, 
  ArrowDownIcon, 
  MagnifyingGlassIcon,
  FunnelIcon 
} from '@heroicons/react/24/outline'

export default function TransactionHistory() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedAccount, setSelectedAccount] = useState('')
  const [selectedType, setSelectedType] = useState('')
  const [dateRange, setDateRange] = useState({
    start: '',
    end: ''
  })
  
  const { accounts } = useAccounts()

  useEffect(() => {
    fetchTransactions()
  }, [selectedAccount, selectedType, dateRange])

  const fetchTransactions = async () => {
    setIsLoading(true)
    try {
      const params = new URLSearchParams()
      if (selectedAccount) params.append('accountNumber', selectedAccount)
      if (selectedType) params.append('type', selectedType)
      if (dateRange.start) params.append('startDate', dateRange.start)
      if (dateRange.end) params.append('endDate', dateRange.end)

      const response = await apiService.get<Transaction[]>(`/transactions?${params.toString()}`)
      setTransactions(response)
    } catch (error) {
      console.error('Failed to fetch transactions:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const filteredTransactions = transactions.filter(transaction =>
    transaction.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
    transaction.transactionNumber.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const getStatusBadge = (status: string) => {
    const statusConfig = {
      COMPLETED: { label: 'Berhasil', variant: 'default' as const },
      PENDING: { label: 'Pending', variant: 'secondary' as const },
      FAILED: { label: 'Gagal', variant: 'destructive' as const },
      REVERSED: { label: 'Dibatalkan', variant: 'outline' as const },
    }
    
    return statusConfig[status as keyof typeof statusConfig] || {
      label: status,
      variant: 'outline' as const
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Riwayat Transaksi</CardTitle>
        <CardDescription>
          Lihat semua transaksi yang telah dilakukan
        </CardDescription>
      </CardHeader>
      
      <CardContent>
        {/* Filters */}
        <div className="space-y-4 mb-6">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <MagnifyingGlassIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                <Input
                  placeholder="Cari transaksi..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            <Button variant="outline" size="sm">
              <FunnelIcon className="w-4 h-4 mr-2" />
              Filter
            </Button>
          </div>
          
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <Select value={selectedAccount} onValueChange={setSelectedAccount}>
              <SelectTrigger>
                <SelectValue placeholder="Semua Rekening" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">Semua Rekening</SelectItem>
                {accounts.map((account) => (
                  <SelectItem key={account.id} value={account.accountNumber}>
                    {account.accountName} ({account.accountNumber})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            
            <Select value={selectedType} onValueChange={setSelectedType}>
              <SelectTrigger>
                <SelectValue placeholder="Semua Jenis" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">Semua Jenis</SelectItem>
                <SelectItem value="CREDIT">Kredit</SelectItem>
                <SelectItem value="DEBIT">Debit</SelectItem>
              </SelectContent>
            </Select>
            
            <div className="flex gap-2">
              <Input
                type="date"
                value={dateRange.start}
                onChange={(e) => setDateRange(prev => ({ ...prev, start: e.target.value }))}
                className="flex-1"
              />
              <Input
                type="date"
                value={dateRange.end}
                onChange={(e) => setDateRange(prev => ({ ...prev, end: e.target.value }))}
                className="flex-1"
              />
            </div>
          </div>
        </div>

        {/* Transaction List */}
        <div className="space-y-4">
          {isLoading ? (
            <div className="space-y-4">
              {Array.from({ length: 5 }).map((_, i) => (
                <div key={i} className="animate-pulse">
                  <div className="flex items-center space-x-4 p-4 border rounded-lg">
                    <div className="w-10 h-10 bg-gray-200 rounded-full"></div>
                    <div className="flex-1 space-y-2">
                      <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                      <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                    </div>
                    <div className="h-4 bg-gray-200 rounded w-20"></div>
                  </div>
                </div>
              ))}
            </div>
          ) : filteredTransactions.length > 0 ? (
            filteredTransactions.map((transaction) => {
              const statusBadge = getStatusBadge(transaction.status)
              
              return (
                <div key={transaction.id} className="flex items-center space-x-4 p-4 border rounded-lg hover:bg-gray-50 transition-colors">
                  <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
                    transaction.transactionType === 'CREDIT' 
                      ? 'bg-green-100' 
                      : 'bg-red-100'
                  }`}>
                    {transaction.transactionType === 'CREDIT' ? (
                      <ArrowUpIcon className="w-5 h-5 text-green-600" />
                    ) : (
                      <ArrowDownIcon className="w-5 h-5 text-red-600" />
                    )}
                  </div>
                  
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-1">
                      <h4 className="text-sm font-medium text-gray-900 truncate">
                        {transaction.description}
                      </h4>
                      <Badge variant={statusBadge.variant} className="ml-2">
                        {statusBadge.label}
                      </Badge>
                    </div>
                    
                    <div className="flex items-center justify-between text-sm text-gray-500">
                      <div>
                        <span>{transaction.transactionNumber}</span>
                        <span className="mx-2">•</span>
                        <span>{formatDate(transaction.transactionDate)}</span>
                      </div>
                      <div>
                        <span>{transaction.accountNumber}</span>
                        {transaction.toAccountNumber && (
                          <>
                            <span className="mx-2">→</span>
                            <span>{transaction.toAccountNumber}</span>
                          </>
                        )}
                      </div>
                    </div>
                  </div>
                  
                  <div className="text-right">
                    <div className={`text-sm font-medium ${
                      transaction.transactionType === 'CREDIT' 
                        ? 'text-green-600' 
                        : 'text-red-600'
                    }`}>
                      {transaction.transactionType === 'CREDIT' ? '+' : '-'}
                      {formatCurrency(transaction.amount)}
                    </div>
                    <div className="text-xs text-gray-500">
                      Saldo: {formatCurrency(transaction.balanceAfter)}
                    </div>
                  </div>
                </div>
              )
            })
          ) : (
            <div className="text-center py-12">
              <div className="w-12 h-12 mx-auto mb-4 text-gray-400">
                <ArrowUpIcon />
              </div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                Tidak ada transaksi
              </h3>
              <p className="text-gray-500">
                {searchTerm || selectedAccount || selectedType || dateRange.start 
                  ? 'Tidak ada transaksi yang sesuai dengan filter'
                  : 'Belum ada transaksi yang dilakukan'
                }
              </p>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
```

## 12. Utility Functions

### 12.1 Formatters
```typescript
// src/utils/formatters.ts
export const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount)
}

export const formatNumber = (number: number): string => {
  return new Intl.NumberFormat('id-ID').format(number)
}

export const formatDate = (dateString: string): string => {
  return new Intl.DateTimeFormat('id-ID', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(new Date(dateString))
}

export const formatDateTime = (dateString: string): string => {
  return new Intl.DateTimeFormat('id-ID', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(dateString))
}

export const formatAccountNumber = (accountNumber: string): string => {
  // Format account number with spaces for better readability
  return accountNumber.replace(/(\d{4})/g, '$1 ').trim()
}
```

### 12.2 Validation Schemas
```typescript
// src/lib/validations.ts
import { z } from 'zod'

export const loginSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  password: z.string().min(8, 'Password minimal 8 karakter'),
})

export const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    .min(8, 'Password minimal 8 karakter')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/, 
      'Password harus mengandung huruf besar, kecil, angka, dan karakter khusus'),
  fullName: z.string().min(2, 'Nama lengkap minimal 2 karakter'),
  phoneNumber: z.string().regex(/^(\+62|62|0)8[1-9][0-9]{6,9}$/, 'Format nomor HP tidak valid').optional(),
  nationalId: z.string().regex(/^[0-9]{16}$/, 'NIK harus 16 digit angka').optional(),
})

export const createAccountSchema = z.object({
  accountTypeId: z.number().min(1, 'Pilih jenis rekening'),
  accountName: z.string().min(1, 'Nama rekening harus diisi'),
  initialDeposit: z.number().min(1, 'Setoran awal harus lebih dari 0'),
  purpose: z.string().optional(),
})

export const transferSchema = z.object({
  fromAccountNumber: z.string().min(1, 'Pilih rekening sumber'),
  toAccountNumber: z.string().min(1, 'Nomor rekening tujuan harus diisi'),
  amount: z.number().min(1, 'Jumlah harus lebih dari 0'),
  description: z.string().min(1, 'Keterangan harus diisi'),
})

export const depositSchema = z.object({
  accountNumber: z.string().min(1, 'Pilih rekening'),
  amount: z.number().min(1, 'Jumlah harus lebih dari 0'),
  description: z.string().min(1, 'Keterangan harus diisi'),
})

export const withdrawalSchema = z.object({
  accountNumber: z.string().min(1, 'Pilih rekening'),
  amount: z.number().min(1, 'Jumlah harus lebih dari 0'),
  description: z.string().min(1, 'Keterangan harus diisi'),
})
```

## 13. Page Components

### 13.1 Dashboard Page
```typescript
// src/app/(dashboard)/page.tsx
import { Metadata } from 'next'
import Header from '@/components/dashboard/Header'
import StatCards from '@/components/dashboard/StatCards'
import RecentTransactions from '@/components/dashboard/RecentTransactions'
import AuthGuard from '@/components/auth/AuthGuard'

export const metadata: Metadata = {
  title: 'Dashboard - Bank Syariah Aisyah',
  description: 'Dashboard perbankan syariah',
}

export default function DashboardPage() {
  return (
    <AuthGuard>
      <div className="min-h-screen bg-gray-50">
        <Header 
          title="Dashboard" 
          description="Selamat datang di Bank Syariah Aisyah"
        />
        
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="space-y-8">
            {/* Statistics Cards */}
            <StatCards 
              totalBalance={25000000}
              totalAccounts={3}
              monthlyIncome={5000000}
              monthlyExpense={3000000}
            />
            
            {/* Recent Transactions */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              <div className="lg:col-span-2">
                <RecentTransactions />
              </div>
              
              {/* Quick Actions */}
              <div className="space-y-6">
                <Card>
                  <CardHeader>
                    <CardTitle>Aksi Cepat</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <Button asChild className="w-full" variant="islamic">
                      <Link href="/dashboard/transactions/transfer">
                        Transfer
                      </Link>
                    </Button>
                    <Button asChild variant="outline" className="w-full">
                      <Link href="/dashboard/transactions/deposit">
                        Setor Tunai
                      </Link>
                    </Button>
                    <Button asChild variant="outline" className="w-full">
                      <Link href="/dashboard/transactions/withdrawal">
                        Tarik Tunai
                      </Link>
                    </Button>
                    <Button asChild variant="outline" className="w-full">
                      <Link href="/dashboard/accounts/create">
                        Buka Rekening
                      </Link>
                    </Button>
                  </CardContent>
                </Card>
                
                {/* Islamic Banking Info */}
                <Card>
                  <CardHeader>
                    <CardTitle className="text-emerald-800">
                      Prinsip Syariah
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3 text-sm text-gray-600">
                      <div className="flex items-start space-x-2">
                        <div className="w-2 h-2 rounded-full bg-emerald-500 mt-2"></div>
                        <div>
                          <p className="font-medium">Bebas Riba</p>
                          <p className="text-xs">Semua produk bebas dari unsur riba</p>
                        </div>
                      </div>
                      <div className="flex items-start space-x-2">
                        <div className="w-2 h-2 rounded-full bg-emerald-500 mt-2"></div>
                        <div>
                          <p className="font-medium">Bagi Hasil</p>
                          <p className="text-xs">Sistem bagi hasil yang adil dan transparan</p>
                        </div>
                      </div>
                      <div className="flex items-start space-x-2">
                        <div className="w-2 h-2 rounded-full bg-emerald-500 mt-2"></div>
                        <div>
                          <p className="font-medium">Halal</p>
                          <p className="text-xs">Investasi pada sektor halal</p>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </div>
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
```

### 13.2 Login Page
```typescript
// src/app/(auth)/login/page.tsx
import { Metadata } from 'next'
import LoginForm from '@/components/auth/LoginForm'

export const metadata: Metadata = {
  title: 'Masuk - Bank Syariah Aisyah',
  description: 'Masuk ke akun Bank Syariah Aisyah',
}

export default function LoginPage() {
  return <LoginForm />
}
```

## 14. Layout Components

### 14.1 Dashboard Layout
```typescript
// src/app/(dashboard)/layout.tsx
import Sidebar from '@/components/dashboard/Sidebar'
import AuthGuard from '@/components/auth/AuthGuard'

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <AuthGuard>
      <div className="h-screen flex overflow-hidden bg-gray-50">
        <Sidebar />
        <div className="flex flex-col w-0 flex-1 overflow-hidden">
          <div className="md:pl-64">
            <div className="flex flex-col flex-1">
              <main className="flex-1 relative overflow-y-auto focus:outline-none">
                {children}
              </main>
            </div>
          </div>
        </div>
      </div>
    </AuthGuard>
  )
}
```

### 14.2 Root Layout
```typescript
// src/app/layout.tsx
import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'
import { Providers } from './providers'
import { Toaster } from '@/components/ui/toaster'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'Bank Syariah Aisyah',
  description: 'Perbankan Syariah Modern untuk Indonesia',
  keywords: ['bank syariah', 'islamic banking', 'halal', 'indonesia'],
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="id">
      <body className={inter.className}>
        <Providers>
          {children}
          <Toaster />
        </Providers>
      </body>
    </html>
  )
}
```

### 14.3 Providers
```typescript
// src/app/providers.tsx
'use client'

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ThemeProvider } from 'next-themes'
import { useState } from 'react'

export function Providers({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(() => new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 5 * 60 * 1000, // 5 minutes
        cacheTime: 10 * 60 * 1000, // 10 minutes
      },
    },
  }))

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider
        attribute="class"
        defaultTheme="light"
        enableSystem
        disableTransitionOnChange
      >
        {children}
      </ThemeProvider>
    </QueryClientProvider>
  )
}
```

## 15. Styling Configuration

### 15.1 Tailwind CSS Configuration
```javascript
// tailwind.config.js
/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: ["class"],
  content: [
    './pages/**/*.{ts,tsx}',
    './components/**/*.{ts,tsx}',
    './app/**/*.{ts,tsx}',
    './src/**/*.{ts,tsx}',
  ],
  theme: {
    container: {
      center: true,
      padding: "2rem",
      screens: {
        "2xl": "1400px",
      },
    },
    extend: {
      colors: {
        border: "hsl(var(--border))",
        input: "hsl(var(--input))",
        ring: "hsl(var(--ring))",
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        primary: {
          DEFAULT: "hsl(var(--primary))",
          foreground: "hsl(var(--primary-foreground))",
        },
        secondary: {
          DEFAULT: "hsl(var(--secondary))",
          foreground: "hsl(var(--secondary-foreground))",
        },
        destructive: {
          DEFAULT: "hsl(var(--destructive))",
          foreground: "hsl(var(--destructive-foreground))",
        },
        muted: {
          DEFAULT: "hsl(var(--muted))",
          foreground: "hsl(var(--muted-foreground))",
        },
        accent: {
          DEFAULT: "hsl(var(--accent))",
          foreground: "hsl(var(--accent-foreground))",
        },
        popover: {
          DEFAULT: "hsl(var(--popover))",
          foreground: "hsl(var(--popover-foreground))",
        },
        card: {
          DEFAULT: "hsl(var(--card))",
          foreground: "hsl(var(--card-foreground))",
        },
        emerald: {
          50: '#ecfdf5',
          100: '#d1fae5',
          200: '#a7f3d0',
          300: '#6ee7b7',
          400: '#34d399',
          500: '#10b981',
          600: '#059669',
          700: '#047857',
          800: '#065f46',
          900: '#064e3b',
        },
      },
      borderRadius: {
        lg: "var(--radius)",
        md: "calc(var(--radius) - 2px)",
        sm: "calc(var(--radius) - 4px)",
      },
      keyframes: {
        "accordion-down": {
          from: { height: 0 },
          to: { height: "var(--radix-accordion-content-height)" },
        },
        "accordion-up": {
          from: { height: "var(--radix-accordion-content-height)" },
          to: { height: 0 },
        },
      },
      animation: {
        "accordion-down": "accordion-down 0.2s ease-out",
        "accordion-up": "accordion-up 0.2s ease-out",
      },
    },
  },
  plugins: [require("tailwindcss-animate")],
}
```

### 15.2 Global CSS
```css
/* src/app/globals.css */
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    --background: 0 0% 100%;
    --foreground: 222.2 84% 4.9%;
    --card: 0 0% 100%;
    --card-foreground: 222.2 84% 4.9%;
    --popover: 0 0% 100%;
    --popover-foreground: 222.2 84% 4.9%;
    --primary: 142.1 76.2% 36.3%;
    --primary-foreground: 355.7 100% 97.3%;
    --secondary: 210 40% 96%;
    --secondary-foreground: 222.2 84% 4.9%;
    --muted: 210 40% 96%;
    --muted-foreground: 215.4 16.3% 46.9%;
    --accent: 210 40% 96%;
    --accent-foreground: 222.2 84% 4.9%;
    --destructive: 0 84.2% 60.2%;
    --destructive-foreground: 210 40% 98%;
    --border: 214.3 31.8% 91.4%;
    --input: 214.3 31.8% 91.4%;
    --ring: 142.1 76.2% 36.3%;
    --radius: 0.5rem;
  }

  .dark {
    --background: 222.2 84% 4.9%;
    --foreground: 210 40% 98%;
    --card: 222.2 84% 4.9%;
    --card-foreground: 210 40% 98%;
    --popover: 222.2 84% 4.9%;
    --popover-foreground: 210 40% 98%;
    --primary: 142.1 70.6% 45.3%;
    --primary-foreground: 144.9 80.4% 10%;
    --secondary: 217.2 32.6% 17.5%;
    --secondary-foreground: 210 40% 98%;
    --muted: 217.2 32.6% 17.5%;
    --muted-foreground: 215 20.2% 65.1%;
    --accent: 217.2 32.6% 17.5%;
    --accent-foreground: 210 40% 98%;
    --destructive: 0 62.8% 30.6%;
    --destructive-foreground: 210 40% 98%;
    --border: 217.2 32.6% 17.5%;
    --input: 217.2 32.6% 17.5%;
    --ring: 142.4 71.8% 29.2%;
  }
}

@layer base {
  * {
    @apply border-border;
  }
  body {
    @apply bg-background text-foreground;
  }
}

/* Islamic Banking Theme Colors */
.islamic-green {
  @apply bg-emerald-600 text-white;
}

.islamic-gold {
  @apply bg-yellow-500 text-white;
}

/* Custom scrollbar */
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

/* Loading animations */
.loading-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: .5;
  }
}

/* Form focus states */
.form-input:focus {
  @apply ring-2 ring-emerald-500 border-emerald-500;
}

/* Islamic pattern background */
.islamic-pattern {
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23f0fdf4' fill-opacity='0.4'%3E%3Ccircle cx='30' cy='30' r='2'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}
```

## 16. Testing Setup

### 16.1 Jest Configuration
```javascript
// jest.config.js
const nextJest = require('next/jest')

const createJestConfig = nextJest({
  dir: './',
})

const customJestConfig = {
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  moduleNameMapping: {
    '^@/components/(.*)const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    .min(8, 'Password minimal 8 karakter')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/, 
      'Password harus mengandung huruf besar, kecil, angka, dan karakter khusus'),
  fullName: z.string().min(2, 'Nama lengkap minimal 2 karakter'),
  phoneNumber: z.string().regex(/^(\+62|62|0)8[1-9][0-9]{6,9}$/, 'Format nomor HP tidak valid').optional(),
  dateOfBirth: z.string().optional(),
  nationalId: z.string().regex(/^[0-9]{16}$/, 'NIK harus 16 digit angka').optional(),
  address: z.string().max(500, 'Alamat maksimal 500 karakter').optional(),
})

export default function RegisterForm() {
  const [isLoading, setIsLoading] = useState(false)
  const { register } = useAuth(false)
  const router = useRouter()

  const form = useForm<RegisterRequest>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      fullName: '',
      phoneNumber: '',
      dateOfBirth: '',
      nationalId: '',
      address: '',
    },
  })

  const onSubmit = async (data: RegisterRequest) => {
    setIsLoading(true)
    try {
      await register(data)
      router.push('/login?message=Registration successful')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Registration failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100 py-8">
      <Card className="w-full max-w-2xl">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Daftar Akun Baru
          </CardTitle>
          <CardDescription>
            Bergabunglah dengan Bank Syariah Aisyah
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="username"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Username *</FormLabel>
                      <FormControl>
                        <Input placeholder="Username" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="email"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Email *</FormLabel>
                      <FormControl>
                        <Input type="email" placeholder="Email" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password *</FormLabel>
                    <FormControl>
                      <Input type="password" placeholder="Password" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="fullName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Nama Lengkap *</FormLabel>
                    <FormControl>
                      <Input placeholder="Nama lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="phoneNumber"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Nomor HP</FormLabel>
                      <FormControl>
                        <Input placeholder="08xxxxxxxxx" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="dateOfBirth"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Tanggal Lahir</FormLabel>
                      <FormControl>
                        <Input type="date" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="nationalId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>NIK (Nomor Induk Kependudukan)</FormLabel>
                    <FormControl>
                      <Input placeholder="16 digit NIK" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="address"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Alamat</FormLabel>
                    <FormControl>
                      <Input placeholder="Alamat lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Mendaftarkan...' : 'Daftar'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Sudah punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/login')}
            >
              Masuk sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.3 Auth Guard Component
```typescript
// src/components/auth/AuthGuard.tsx
'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'
import { Loading } from '@/components/common/Loading'

interface AuthGuardProps {
  children: React.ReactNode
  requireAuth?: boolean
  requiredRole?: string[]
}

export default function AuthGuard({ 
  children, 
  requireAuth = true, 
  requiredRole 
}: AuthGuardProps) {
  const { isAuthenticated, user, isLoading } = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (!isLoading) {
      if (requireAuth && !isAuthenticated) {
        router.push('/login')
        return
      }

      if (requiredRole && user && !requiredRole.includes(user.role)) {
        router.push('/unauthorized')
        return
      }
    }
  }, [isAuthenticated, user, isLoading, requireAuth, requiredRole, router])

  if (isLoading) {
    return <Loading />
  }

  if (requireAuth && !isAuthenticated) {
    return null
  }

  if (requiredRole && user && !requiredRole.includes(user.role)) {
    return null
  }

  return <>{children}</>
}
```

## 9. Dashboard Components

### 9.1 Dashboard Layout
```typescript
// src/components/dashboard/Sidebar.tsx
'use client'

import { useState } from 'react'
import { usePathname } from 'next/navigation'
import Link from 'next/link'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/hooks/useAuth'
import {
  HomeIcon,
  CreditCardIcon,
  ArrowsRightLeftIcon,
  ChartBarIcon,
  UserIcon,
  Bars3Icon,
  XMarkIcon,
} from '@heroicons/react/24/outline'

const navigation = [
  { name: 'Dashboard', href: '/dashboard', icon: HomeIcon },
  { name: 'Rekening', href: '/dashboard/accounts', icon: CreditCardIcon },
  { name: 'Transaksi', href: '/dashboard/transactions', icon: ArrowsRightLeftIcon },
  { name: 'Laporan', href: '/dashboard/reports', icon: ChartBarIcon },
  { name: 'Profile', href: '/dashboard/profile', icon: UserIcon },
]

export default function Sidebar() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const pathname = usePathname()
  const { user, logout } = useAuth()

  return (
    <>
      {/* Mobile sidebar */}
      <div className={cn(
        'fixed inset-0 flex z-40 md:hidden',
        sidebarOpen ? 'block' : 'hidden'
      )}>
        <div className="fixed inset-0 bg-gray-600 bg-opacity-75" onClick={() => setSidebarOpen(false)} />
        <div className="relative flex-1 flex flex-col max-w-xs w-full bg-white">
          <div className="absolute top-0 right-0 -mr-12 pt-2">
            <Button
              variant="ghost"
              onClick={() => setSidebarOpen(false)}
              className="ml-1 flex items-center justify-center h-10 w-10 rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
            >
              <XMarkIcon className="h-6 w-6 text-white" />
            </Button>
          </div>
          <SidebarContent />
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className="hidden md:flex md:w-64 md:flex-col md:fixed md:inset-y-0">
        <div className="flex-1 flex flex-col min-h-0 border-r border-gray-200 bg-white">
          <SidebarContent />
        </div>
      </div>

      {/* Mobile menu button */}
      <div className="sticky top-0 z-10 md:hidden pl-1 pt-1 sm:pl-3 sm:pt-3 bg-white border-b">
        <Button
          variant="ghost"
          onClick={() => setSidebarOpen(true)}
          className="-ml-0.5 -mt-0.5 h-12 w-12 inline-flex items-center justify-center"
        >
          <Bars3Icon className="h-6 w-6" />
        </Button>
      </div>
    </>
  )

  function SidebarContent() {
    return (
      <>
        <div className="flex-1 flex flex-col pt-5 pb-4 overflow-y-auto">
          <div className="flex items-center flex-shrink-0 px-4">
            <h2 className="text-lg font-semibold text-emerald-800">
              Bank Syariah Aisyah
            </h2>
          </div>
          <nav className="mt-5 flex-1 px-2 space-y-1">
            {navigation.map((item) => {
              const isActive = pathname === item.href
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={cn(
                    'group flex items-center px-2 py-2 text-sm font-medium rounded-md',
                    isActive
                      ? 'bg-emerald-100 text-emerald-900'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  )}
                >
                  <item.icon
                    className={cn(
                      'mr-3 flex-shrink-0 h-6 w-6',
                      isActive ? 'text-emerald-500' : 'text-gray-400 group-hover:text-gray-500'
                    )}
                  />
                  {item.name}
                </Link>
              )
            })}
          </nav>
        </div>
        
        {/* User info and logout */}
        <div className="flex-shrink-0 flex border-t border-gray-200 p-4">
          <div className="flex-shrink-0 w-full group block">
            <div className="flex items-center">
              <div>
                <div className="inline-flex items-center justify-center h-9 w-9 rounded-full bg-emerald-500">
                  <span className="text-sm font-medium leading-none text-white">
                    {user?.fullName?.charAt(0).toUpperCase()}
                  </span>
                </div>
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-700">{user?.fullName}</p>
                <Button
                  variant="link"
                  onClick={logout}
                  className="text-xs text-gray-500 p-0 h-auto"
                >
                  Keluar
                </Button>
              </div>
            </div>
          </div>
        </div>
      </>
    )
  }
}
```

### 9.2 Dashboard Header
```typescript
// src/components/dashboard/Header.tsx
'use client'

import { Bell, Search } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

interface HeaderProps {
  title: string
  description?: string
}

export default function Header({ title, description }: HeaderProps) {
  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center py-6">
          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold leading-7 text-gray-900 sm:truncate">
              {title}
            </h1>
            {description && (
              <p className="mt-1 text-sm text-gray-500">
                {description}
              </p>
            )}
          </div>
          
          <div className="flex items-center space-x-4">
            <div className="flex-1 max-w-lg">
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Search className="h-5 w-5 text-gray-400" />
                </div>
                <Input
                  type="search"
                  placeholder="Cari..."
                  className="pl-10 pr-4 py-2 w-full"
                />
              </div>
            </div>
            
            <Button variant="ghost" size="icon">
              <Bell className="h-5 w-5" />
            </Button>
          </div>
        </div>
      </div>
    </header>
  )
}
```

### 9.3 Statistics Cards
```typescript
// src/components/dashboard/StatCards.tsx
'use client'

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { formatCurrency } from '@/utils/formatters'
import { 
  CreditCardIcon, 
  ArrowUpIcon, 
  ArrowDownIcon,
  BanknotesIcon 
} from '@heroicons/react/24/outline'

interface StatCard {
  title: string
  value: string
  change?: string
  changeType?: 'increase' | 'decrease'
  icon: React.ComponentType<any>
}

interface StatCardsProps {
  totalBalance: number
  totalAccounts: number
  monthlyIncome: number
  monthlyExpense: number
}

export default function StatCards({ 
  totalBalance, 
  totalAccounts, 
  monthlyIncome, 
  monthlyExpense 
}: StatCardsProps) {
  const stats: StatCard[] = [
    {
      title: 'Total Saldo',
      value: formatCurrency(totalBalance),
      icon: BanknotesIcon,
    },
    {
      title: 'Jumlah Rekening',
      value: totalAccounts.toString(),
      icon: CreditCardIcon,
    },
    {
      title: 'Pemasukan Bulan Ini',
      value: formatCurrency(monthlyIncome),
      change: '+12%',
      changeType: 'increase',
      icon: ArrowUpIcon,
    },
    {
      title: 'Pengeluaran Bulan Ini',
      value: formatCurrency(monthlyExpense),
      change: '-5%',
      changeType: 'decrease',
      icon: ArrowDownIcon,
    },
  ]

  return (
    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
      {stats.map((stat) => (
        <Card key={stat.title}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {stat.title}
            </CardTitle>
            <stat.icon className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stat.value}</div>
            {stat.change && (
              <p className={`text-xs ${
                stat.changeType === 'increase' ? 'text-green-600' : 'text-red-600'
              }`}>
                {stat.change} dari bulan lalu
              </p>
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
```

### 9.4 Recent Transactions Component
```typescript
// src/components/dashboard/RecentTransactions.tsx
'use client'

import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Transaction } from '@/types/transaction'
import { apiService } from '@/lib/api'
import { formatCurrency, formatDate } from '@/utils/formatters'
import { ArrowUpIcon, ArrowDownIcon } from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function RecentTransactions() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchRecentTransactions = async () => {
      try {
        const response = await apiService.get<Transaction[]>('/transactions/recent?limit=5')
        setTransactions(response)
      } catch (error) {
        console.error('Failed to fetch recent transactions:', error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchRecentTransactions()
  }, [])

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Transaksi Terbaru</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="flex items-center space-x-4">
                  <div className="w-10 h-10 bg-gray-200 rounded-full"></div>
                  <div className="flex-1 space-y-2">
                    <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                    <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                  </div>
                  <div className="h-4 bg-gray-200 rounded w-20"></div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <div>
          <CardTitle>Transaksi Terbaru</CardTitle>
          <CardDescription>
            {transactions.length} transaksi terakhir
          </CardDescription>
        </div>
        <Button asChild variant="outline" size="sm">
          <Link href="/dashboard/transactions">
            Lihat Semua
          </Link>
        </Button>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {transactions.map((transaction) => (
            <div key={transaction.id} className="flex items-center space-x-4">
              <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
                transaction.transactionType === 'CREDIT' 
                  ? 'bg-green-100' 
                  : 'bg-red-100'
              }`}>
                {transaction.transactionType === 'CREDIT' ? (
                  <ArrowUpIcon className="w-5 h-5 text-green-600" />
                ) : (
                  <ArrowDownIcon className="w-5 h-5 text-red-600" />
                )}
              </div>
              
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 truncate">
                  {transaction.description}
                </p>
                <p className="text-sm text-gray-500">
                  {formatDate(transaction.transactionDate)} • {transaction.accountNumber}
                </p>
              </div>
              
              <div className="text-right">
                <p className={`text-sm font-medium ${
                  transaction.transactionType === 'CREDIT' 
                    ? 'text-green-600' 
                    : 'text-red-600'
                }`}>
                  {transaction.transactionType === 'CREDIT' ? '+' : '-'}
                  {formatCurrency(transaction.amount)}
                </p>
                <p className="text-xs text-gray-500">
                  {transaction.status}
                </p>
              </div>
            </div>
          ))}
          
          {transactions.length === 0 && (
            <div className="text-center py-6">
              <p className="text-gray-500">Belum ada transaksi</p>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
```

## 10. Account Management Components

### 10.1 Account List
```typescript
// src/components/accounts/AccountList.tsx
'use client'

import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Account } from '@/types/account'
import { formatCurrency } from '@/utils/formatters'
import { useAccounts } from '@/hooks/useAccounts'
import { 
  CreditCardIcon, 
  EyeIcon, 
  PlusIcon 
} from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function AccountList() {
  const { accounts, isLoading } = useAccounts()
  const [visibleBalances, setVisibleBalances] = useState<Record<number, boolean>>({})

  const toggleBalanceVisibility = (accountId: number) => {
    setVisibleBalances(prev => ({
      ...prev,
      [accountId]: !prev[accountId]
    }))
  }

  const getStatusBadge = (status: string) => {
    const statusConfig = {
      ACTIVE: { label: 'Aktif', variant: 'default' as const },
      SUSPENDED: { label: 'Ditangguhkan', variant: 'secondary' as const },
      CLOSED: { label: 'Ditutup', variant: 'destructive' as const },
      FROZEN: { label: 'Dibekukan', variant: 'outline' as const },
    }
    
    return statusConfig[status as keyof typeof statusConfig] || {
      label: status,
      variant: 'outline' as const
    }
  }

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Array.from({ length: 3 }).map((_, i) => (
          <Card key={i} className="animate-pulse">
            <CardHeader>
              <div className="h-4 bg-gray-200 rounded w-3/4"></div>
              <div className="h-3 bg-gray-200 rounded w-1/2"></div>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="h-6 bg-gray-200 rounded w-full"></div>
                <div className="h-4 bg-gray-200 rounded w-2/3"></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold">Rekening Saya</h2>
        <Button asChild>
          <Link href="/dashboard/accounts/create">
            <PlusIcon className="w-4 h-4 mr-2" />
            Buka Rekening Baru
          </Link>
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {accounts.map((account) => {
          const statusBadge = getStatusBadge(account.status)
          const isBalanceVisible = visibleBalances[account.id]
          
          return (
            <Card key={account.id} className="hover:shadow-md transition-shadow">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="p-2 bg-emerald-100 rounded-lg">
                      <CreditCardIcon className="w-5 h-5 text-emerald-600" />
                    </div>
                    <div>
                      <CardTitle className="text-sm font-medium">
                        {account.accountName}
                      </CardTitle>
                      <p className="text-xs text-gray-500">
                        {account.accountNumber}
                      </p>
                    </div>
                  </div>
                  <Badge variant={statusBadge.variant}>
                    {statusBadge.label}
                  </Badge>
                </div>
              </CardHeader>
              
              <CardContent className="pt-0">
                <div className="space-y-3">
                  <div>
                    <p className="text-xs text-gray-500 mb-1">Saldo</p>
                    <div className="flex items-center justify-between">
                      <p className="text-lg font-bold">
                        {isBalanceVisible 
                          ? formatCurrency(account.balance)
                          : '••••••••'
                        }
                      </p>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => toggleBalanceVisibility(account.id)}
                      >
                        <EyeIcon className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  
                  <div className="flex justify-between text-xs text-gray-500">
                    <span>Jenis: {account.accountTypeName}</span>
                    <span>{account.currency}</span>
                  </div>
                  
                  <div className="flex space-x-2">
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/accounts/${account.id}`}>
                        Detail
                      </Link>
                    </Button>
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/transactions?account=${account.accountNumber}`}>
                        Transaksi
                      </Link>
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
        
        {accounts.length === 0 && (
          <Card className="col-span-full">
            <CardContent className="text-center py-12">
              <CreditCardIcon className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                Belum ada rekening
              </h3>
              <p className="text-gray-500 mb-6">
                Buka rekening pertama Anda untuk mulai bertransaksi
              </p>
              <Button asChild>
                <Link href="/dashboard/accounts/create">
                  Buka Rekening Sekarang
                </Link>
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
```

## 11. Transaction Components

### 11.1 Transfer Form
```typescript
// src/components/transactions/TransferForm.tsx
'use client'

import { useState } from 'react'
import { useForm# Frontend Guide - Bank Syariah Aisyah Application (Next.js)

## 1. Project Setup

### 1.1 Initialize Next.js Project
```bash
npx create-next-app@latest bank-syariah-aisyah-frontend --typescript --tailwind --eslint --app
cd bank-syariah-aisyah-frontend
```

### 1.2 Package.json Configuration
```json
{
  "name": "bank-syariah-aisyah-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "type-check": "tsc --noEmit",
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage"
  },
  "dependencies": {
    "@headlessui/react": "^1.7.17",
    "@heroicons/react": "^2.0.18",
    "@hookform/resolvers": "^3.3.2",
    "@radix-ui/react-avatar": "^1.0.4",
    "@radix-ui/react-dialog": "^1.0.5",
    "@radix-ui/react-dropdown-menu": "^2.0.6",
    "@radix-ui/react-label": "^2.0.2",
    "@radix-ui/react-popover": "^1.0.7",
    "@radix-ui/react-select": "^2.0.0",
    "@radix-ui/react-slot": "^1.0.2",
    "@radix-ui/react-tabs": "^1.0.4",
    "@radix-ui/react-toast": "^1.1.5",
    "@tanstack/react-query": "^5.8.4",
    "@types/node": "^20",
    "@types/react": "^18",
    "@types/react-dom": "^18",
    "autoprefixer": "^10",
    "axios": "^1.6.2",
    "class-variance-authority": "^0.7.0",
    "clsx": "^2.0.0",
    "date-fns": "^2.30.0",
    "framer-motion": "^10.16.5",
    "js-cookie": "^3.0.5",
    "lucide-react": "^0.294.0",
    "next": "14.0.3",
    "next-auth": "^4.24.5",
    "next-themes": "^0.2.1",
    "react": "^18",
    "react-dom": "^18",
    "react-hook-form": "^7.48.2",
    "recharts": "^2.8.0",
    "tailwind-merge": "^2.0.0",
    "tailwindcss-animate": "^1.0.7",
    "typescript": "^5",
    "zod": "^3.22.4",
    "zustand": "^4.4.7"
  },
  "devDependencies": {
    "@types/js-cookie": "^3.0.6",
    "@types/jest": "^29.5.8",
    "eslint": "^8",
    "eslint-config-next": "14.0.3",
    "jest": "^29.7.0",
    "jest-environment-jsdom": "^29.7.0",
    "postcss": "^8",
    "tailwindcss": "^3.3.6"
  }
}
```

### 1.3 Next.js Configuration
```typescript
// next.config.js
/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    appDir: true,
  },
  images: {
    domains: ['localhost', 'api.aisyahbank.com'],
  },
  env: {
    NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL,
    NEXT_PUBLIC_APP_NAME: 'Bank Syariah Aisyah',
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${process.env.NEXT_PUBLIC_API_BASE_URL}/:path*`,
      },
    ]
  },
}

module.exports = nextConfig
```

### 1.4 Environment Variables
```bash
# .env.local
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_APP_NAME=Bank Syariah Aisyah
NEXTAUTH_SECRET=your-nextauth-secret
NEXTAUTH_URL=http://localhost:3000
```

## 2. Project Structure

```
src/
├── app/
│   ├── (auth)/
│   │   ├── login/
│   │   │   └── page.tsx
│   │   ├── register/
│   │   │   └── page.tsx
│   │   └── layout.tsx
│   ├── (dashboard)/
│   │   ├── accounts/
│   │   │   ├── page.tsx
│   │   │   ├── create/
│   │   │   │   └── page.tsx
│   │   │   └── [id]/
│   │   │       └── page.tsx
│   │   ├── transactions/
│   │   │   ├── page.tsx
│   │   │   ├── transfer/
│   │   │   │   └── page.tsx
│   │   │   ├── deposit/
│   │   │   │   └── page.tsx
│   │   │   └── withdrawal/
│   │   │       └── page.tsx
│   │   ├── reports/
│   │   │   └── page.tsx
│   │   ├── profile/
│   │   │   └── page.tsx
│   │   ├── page.tsx
│   │   └── layout.tsx
│   ├── globals.css
│   ├── layout.tsx
│   ├── loading.tsx
│   ├── error.tsx
│   └── not-found.tsx
├── components/
│   ├── ui/
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── dialog.tsx
│   │   ├── form.tsx
│   │   ├── input.tsx
│   │   ├── label.tsx
│   │   ├── select.tsx
│   │   ├── table.tsx
│   │   ├── tabs.tsx
│   │   └── toast.tsx
│   ├── auth/
│   │   ├── LoginForm.tsx
│   │   ├── RegisterForm.tsx
│   │   └── AuthGuard.tsx
│   ├── dashboard/
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   ├── StatCards.tsx
│   │   └── RecentTransactions.tsx
│   ├── accounts/
│   │   ├── AccountCard.tsx
│   │   ├── AccountList.tsx
│   │   └── CreateAccountForm.tsx
│   ├── transactions/
│   │   ├── TransactionList.tsx
│   │   ├── TransactionForm.tsx
│   │   ├── TransferForm.tsx
│   │   ├── DepositForm.tsx
│   │   └── WithdrawalForm.tsx
│   └── common/
│       ├── Loading.tsx
│       ├── ErrorBoundary.tsx
│       └── Layout.tsx
├── lib/
│   ├── api.ts
│   ├── auth.ts
│   ├── utils.ts
│   ├── validations.ts
│   └── constants.ts
├── hooks/
│   ├── useAuth.ts
│   ├── useAccounts.ts
│   ├── useTransactions.ts
│   ├── useLocalStorage.ts
│   └── useDebounce.ts
├── store/
│   ├── authStore.ts
│   ├── accountStore.ts
│   └── transactionStore.ts
├── types/
│   ├── auth.ts
│   ├── account.ts
│   ├── transaction.ts
│   └── api.ts
└── utils/
    ├── formatters.ts
    ├── validators.ts
    └── helpers.ts
```

## 3. UI Components with shadcn/ui

### 3.1 Install shadcn/ui
```bash
npx shadcn-ui@latest init
npx shadcn-ui@latest add button card dialog form input label select table tabs toast
```

### 3.2 Button Component
```typescript
// src/components/ui/button.tsx
import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "@/lib/utils"

const buttonVariants = cva(
  "inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
        outline: "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
        secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline",
        islamic: "bg-emerald-600 text-white hover:bg-emerald-700",
      },
      size: {
        default: "h-10 px-4 py-2",
        sm: "h-9 rounded-md px-3",
        lg: "h-11 rounded-md px-8",
        icon: "h-10 w-10",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button"
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    )
  }
)
Button.displayName = "Button"

export { Button, buttonVariants }
```

### 3.3 Card Component
```typescript
// src/components/ui/card.tsx
import * as React from "react"
import { cn } from "@/lib/utils"

const Card = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn(
      "rounded-lg border bg-card text-card-foreground shadow-sm",
      className
    )}
    {...props}
  />
))
Card.displayName = "Card"

const CardHeader = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex flex-col space-y-1.5 p-6", className)}
    {...props}
  />
))
CardHeader.displayName = "CardHeader"

const CardTitle = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLHeadingElement>
>(({ className, ...props }, ref) => (
  <h3
    ref={ref}
    className={cn(
      "text-2xl font-semibold leading-none tracking-tight",
      className
    )}
    {...props}
  />
))
CardTitle.displayName = "CardTitle"

const CardDescription = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => (
  <p
    ref={ref}
    className={cn("text-sm text-muted-foreground", className)}
    {...props}
  />
))
CardDescription.displayName = "CardDescription"

const CardContent = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div ref={ref} className={cn("p-6 pt-0", className)} {...props} />
))
CardContent.displayName = "CardContent"

const CardFooter = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex items-center p-6 pt-0", className)}
    {...props}
  />
))
CardFooter.displayName = "CardFooter"

export { Card, CardHeader, CardFooter, CardTitle, CardDescription, CardContent }
```

## 4. Type Definitions

### 4.1 Authentication Types
```typescript
// src/types/auth.ts
export interface User {
  id: number
  username: string
  email: string
  fullName: string
  role: 'ADMIN' | 'MANAGER' | 'TELLER' | 'CUSTOMER'
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
  emailVerified: boolean
  phoneVerified: boolean
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  fullName: string
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface AuthState {
  user: User | null
  token: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoading: boolean
}
```

### 4.2 Account Types
```typescript
// src/types/account.ts
export interface Account {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  accountTypeCode: string
  balance: number
  availableBalance: number
  currency: string
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED' | 'FROZEN'
  openedDate: string
  closedDate?: string
  customerNumber: string
  customerName: string
  profitSharingPercentage?: number
  isDormant: boolean
  lastTransactionDate?: string
}

export interface AccountType {
  id: number
  typeCode: string
  typeName: string
  description: string
  minimumBalance: number
  maintenanceFee: number
  profitSharingRatio?: number
  isSavings: boolean
  isActive: boolean
}

export interface CreateAccountRequest {
  accountTypeId: number
  accountName: string
  initialDeposit: number
  purpose?: string
}

export interface AccountResponse {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  balance: number
  status: string
  openedDate: string
}
```

### 4.3 Transaction Types
```typescript
// src/types/transaction.ts
export interface Transaction {
  id: number
  transactionNumber: string
  accountNumber: string
  accountName: string
  categoryName: string
  amount: number
  transactionType: 'CREDIT' | 'DEBIT'
  description: string
  balanceBefore: number
  balanceAfter: number
  transactionDate: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REVERSED'
  channel: 'TELLER' | 'ATM' | 'MOBILE' | 'WEB'
  toAccountNumber?: string
  toAccountName?: string
}

export interface DepositRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface WithdrawalRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface TransferRequest {
  fromAccountNumber: string
  toAccountNumber: string
  amount: number
  description: string
}

export interface TransactionResponse {
  id: number
  transactionNumber: string
  amount: number
  description: string
  transactionDate: string
  status: string
  balanceAfter: number
}
```

## 5. API Service Layer

### 5.1 API Configuration
```typescript
// src/lib/api.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { getToken, removeTokens } from './auth'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1'

class ApiService {
  private api: AxiosInstance

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 10000,
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    // Request interceptor to add auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = getToken()
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Response interceptor to handle auth errors
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          removeTokens()
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }
    )
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.get(url, config)
    return response.data
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.post(url, data, config)
    return response.data
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.put(url, data, config)
    return response.data
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.delete(url, config)
    return response.data
  }
}

export const apiService = new ApiService()
```

### 5.2 Authentication Service
```typescript
// src/lib/auth.ts
import { apiService } from './api'
import { LoginRequest, RegisterRequest, LoginResponse } from '@/types/auth'
import Cookies from 'js-cookie'

const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEY = 'user'

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiService.post<LoginResponse>('/auth/login', credentials)
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  async register(userData: RegisterRequest): Promise<void> {
    await apiService.post('/auth/register', userData)
  },

  async refreshToken(): Promise<LoginResponse> {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      throw new Error('No refresh token available')
    }
    
    const response = await apiService.post<LoginResponse>('/auth/refresh', { refreshToken })
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  logout() {
    removeTokens()
    removeUser()
  }
}

// Token management functions
export const setTokens = (token: string, refreshToken: string) => {
  Cookies.set(TOKEN_KEY, token, { expires: 1 }) // 1 day
  Cookies.set(REFRESH_TOKEN_KEY, refreshToken, { expires: 7 }) // 7 days
}

export const getToken = (): string | undefined => {
  return Cookies.get(TOKEN_KEY)
}

export const getRefreshToken = (): string | undefined => {
  return Cookies.get(REFRESH_TOKEN_KEY)
}

export const removeTokens = () => {
  Cookies.remove(TOKEN_KEY)
  Cookies.remove(REFRESH_TOKEN_KEY)
}

export const setUser = (user: any) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const getUser = () => {
  if (typeof window === 'undefined') return null
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}

export const removeUser = () => {
  localStorage.removeItem(USER_KEY)
}

export const isAuthenticated = (): boolean => {
  return !!getToken()
}
```

## 6. State Management with Zustand

### 6.1 Auth Store
```typescript
// src/store/authStore.ts
import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'
import { User, LoginRequest, RegisterRequest, AuthState } from '@/types/auth'
import { authService } from '@/lib/auth'

interface AuthStore extends AuthState {
  login: (credentials: LoginRequest) => Promise<void>
  register: (userData: RegisterRequest) => Promise<void>
  logout: () => void
  refreshToken: () => Promise<void>
  setUser: (user: User) => void
  setToken: (token: string, refreshToken: string) => void
  clearAuth: () => void
}

export const useAuthStore = create<AuthStore>()(
  devtools(
    persist(
      (set, get) => ({
        user: null,
        token: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,

        login: async (credentials: LoginRequest) => {
          set({ isLoading: true })
          try {
            const response = await authService.login(credentials)
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
              isLoading: false,
            })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        register: async (userData: RegisterRequest) => {
          set({ isLoading: true })
          try {
            await authService.register(userData)
            set({ isLoading: false })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        logout: () => {
          authService.logout()
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },

        refreshToken: async () => {
          try {
            const response = await authService.refreshToken()
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
            })
          } catch (error) {
            get().logout()
            throw error
          }
        },

        setUser: (user: User) => {
          set({ user })
        },

        setToken: (token: string, refreshToken: string) => {
          set({ token, refreshToken, isAuthenticated: true })
        },

        clearAuth: () => {
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },
      }),
      {
        name: 'auth-storage',
        partialize: (state) => ({
          user: state.user,
          token: state.token,
          refreshToken: state.refreshToken,
          isAuthenticated: state.isAuthenticated,
        }),
      }
    )
  )
)
```

### 6.2 Account Store
```typescript
// src/store/accountStore.ts
import { create } from 'zustand'
import { devtools } from 'zustand/middleware'
import { Account, AccountType, CreateAccountRequest } from '@/types/account'
import { apiService } from '@/lib/api'

interface AccountState {
  accounts: Account[]
  accountTypes: AccountType[]
  selectedAccount: Account | null
  isLoading: boolean
  error: string | null
}

interface AccountActions {
  fetchAccounts: () => Promise<void>
  fetchAccountTypes: () => Promise<void>
  createAccount: (request: CreateAccountRequest) => Promise<void>
  getAccountById: (id: number) => Promise<void>
  getAccountByNumber: (accountNumber: string) => Promise<Account>
  setSelectedAccount: (account: Account | null) => void
  clearError: () => void
}

type AccountStore = AccountState & AccountActions

export const useAccountStore = create<AccountStore>()(
  devtools(
    (set, get) => ({
      accounts: [],
      accountTypes: [],
      selectedAccount: null,
      isLoading: false,
      error: null,

      fetchAccounts: async () => {
        set({ isLoading: true, error: null })
        try {
          const accounts = await apiService.get<Account[]>('/accounts/my-accounts')
          set({ accounts, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch accounts',
            isLoading: false 
          })
        }
      },

      fetchAccountTypes: async () => {
        try {
          const accountTypes = await apiService.get<AccountType[]>('/account-types')
          set({ accountTypes })
        } catch (error: any) {
          set({ error: error.response?.data?.message || 'Failed to fetch account types' })
        }
      },

      createAccount: async (request: CreateAccountRequest) => {
        set({ isLoading: true, error: null })
        try {
          const newAccount = await apiService.post<Account>('/accounts', request)
          set(state => ({ 
            accounts: [...state.accounts, newAccount],
            isLoading: false 
          }))
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to create account',
            isLoading: false 
          })
          throw error
        }
      },

      getAccountById: async (id: number) => {
        set({ isLoading: true, error: null })
        try {
          const account = await apiService.get<Account>(`/accounts/${id}`)
          set({ selectedAccount: account, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch account',
            isLoading: false 
          })
        }
      },

      getAccountByNumber: async (accountNumber: string) => {
        const response = await apiService.get<Account>(`/accounts/number/${accountNumber}`)
        return response
      },

      setSelectedAccount: (account: Account | null) => {
        set({ selectedAccount: account })
      },

      clearError: () => {
        set({ error: null })
      },
    })
  )
)
```

## 7. Custom Hooks

### 7.1 useAuth Hook
```typescript
// src/hooks/useAuth.ts
import { useAuthStore } from '@/store/authStore'
import { useRouter } from 'next/navigation'
import { useEffect } from 'react'

export const useAuth = (requireAuth = true) => {
  const authStore = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (requireAuth && !authStore.isAuthenticated) {
      router.push('/login')
    }
  }, [authStore.isAuthenticated, requireAuth, router])

  return {
    user: authStore.user,
    isAuthenticated: authStore.isAuthenticated,
    isLoading: authStore.isLoading,
    login: authStore.login,
    register: authStore.register,
    logout: authStore.logout,
    refreshToken: authStore.refreshToken,
  }
}
```

### 7.2 useAccounts Hook
```typescript
// src/hooks/useAccounts.ts
import { useAccountStore } from '@/store/accountStore'
import { useEffect } from 'react'

export const useAccounts = () => {
  const accountStore = useAccountStore()

  useEffect(() => {
    accountStore.fetchAccounts()
    accountStore.fetchAccountTypes()
  }, [])

  return {
    accounts: accountStore.accounts,
    accountTypes: accountStore.accountTypes,
    selectedAccount: accountStore.selectedAccount,
    isLoading: accountStore.isLoading,
    error: accountStore.error,
    createAccount: accountStore.createAccount,
    getAccountById: accountStore.getAccountById,
    getAccountByNumber: accountStore.getAccountByNumber,
    setSelectedAccount: accountStore.setSelectedAccount,
    clearError: accountStore.clearError,
  }
}
```

### 7.3 useTransactions Hook
```typescript
// src/hooks/useTransactions.ts
import { useState, useEffect } from 'react'
import { Transaction, DepositRequest, WithdrawalRequest, TransferRequest } from '@/types/transaction'
import { apiService } from '@/lib/api'

export const useTransactions = (accountId?: number) => {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchTransactions = async (accountId?: number) => {
    setIsLoading(true)
    setError(null)
    try {
      const url = accountId ? `/transactions/account/${accountId}` : '/transactions/my-transactions'
      const response = await apiService.get<Transaction[]>(url)
      setTransactions(response)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch transactions')
    } finally {
      setIsLoading(false)
    }
  }

  const deposit = async (request: DepositRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/deposit', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process deposit')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const withdraw = async (request: WithdrawalRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/withdrawal', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process withdrawal')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const transfer = async (request: TransferRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/transfer', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process transfer')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchTransactions(accountId)
  }, [accountId])

  return {
    transactions,
    isLoading,
    error,
    fetchTransactions,
    deposit,
    withdraw,
    transfer,
  }
}
```

## 8. Authentication Components

### 8.1 Login Form
```typescript
// src/components/auth/LoginForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { LoginRequest } from '@/types/auth'
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/24/outline'

const loginSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

export default function LoginForm() {
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const { login } = useAuth(false)
  const router = useRouter()

  const form = useForm<LoginRequest>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  })

  const onSubmit = async (data: LoginRequest) => {
    setIsLoading(true)
    try {
      await login(data)
      router.push('/dashboard')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Login failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Bank Syariah Aisyah
          </CardTitle>
          <CardDescription>
            Masuk ke akun perbankan syariah Anda
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Username</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Masukkan username"
                        {...field}
                        disabled={isLoading}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password</FormLabel>
                    <FormControl>
                      <div className="relative">
                        <Input
                          type={showPassword ? 'text' : 'password'}
                          placeholder="Masukkan password"
                          {...field}
                          disabled={isLoading}
                        />
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                          onClick={() => setShowPassword(!showPassword)}
                          disabled={isLoading}
                        >
                          {showPassword ? (
                            <EyeSlashIcon className="h-4 w-4" />
                          ) : (
                            <EyeIcon className="h-4 w-4" />
                          )}
                        </Button>
                      </div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Memproses...' : 'Masuk'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Belum punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/register')}
            >
              Daftar sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.2 Register Form
```typescript
// src/components/auth/RegisterForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { RegisterRequest } from '@/types/auth'

const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    : '<rootDir>/components/$1',
    '^@/pages/(.*)const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    .min(8, 'Password minimal 8 karakter')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/, 
      'Password harus mengandung huruf besar, kecil, angka, dan karakter khusus'),
  fullName: z.string().min(2, 'Nama lengkap minimal 2 karakter'),
  phoneNumber: z.string().regex(/^(\+62|62|0)8[1-9][0-9]{6,9}$/, 'Format nomor HP tidak valid').optional(),
  dateOfBirth: z.string().optional(),
  nationalId: z.string().regex(/^[0-9]{16}$/, 'NIK harus 16 digit angka').optional(),
  address: z.string().max(500, 'Alamat maksimal 500 karakter').optional(),
})

export default function RegisterForm() {
  const [isLoading, setIsLoading] = useState(false)
  const { register } = useAuth(false)
  const router = useRouter()

  const form = useForm<RegisterRequest>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      fullName: '',
      phoneNumber: '',
      dateOfBirth: '',
      nationalId: '',
      address: '',
    },
  })

  const onSubmit = async (data: RegisterRequest) => {
    setIsLoading(true)
    try {
      await register(data)
      router.push('/login?message=Registration successful')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Registration failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100 py-8">
      <Card className="w-full max-w-2xl">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Daftar Akun Baru
          </CardTitle>
          <CardDescription>
            Bergabunglah dengan Bank Syariah Aisyah
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="username"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Username *</FormLabel>
                      <FormControl>
                        <Input placeholder="Username" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="email"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Email *</FormLabel>
                      <FormControl>
                        <Input type="email" placeholder="Email" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password *</FormLabel>
                    <FormControl>
                      <Input type="password" placeholder="Password" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="fullName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Nama Lengkap *</FormLabel>
                    <FormControl>
                      <Input placeholder="Nama lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="phoneNumber"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Nomor HP</FormLabel>
                      <FormControl>
                        <Input placeholder="08xxxxxxxxx" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="dateOfBirth"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Tanggal Lahir</FormLabel>
                      <FormControl>
                        <Input type="date" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="nationalId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>NIK (Nomor Induk Kependudukan)</FormLabel>
                    <FormControl>
                      <Input placeholder="16 digit NIK" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="address"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Alamat</FormLabel>
                    <FormControl>
                      <Input placeholder="Alamat lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Mendaftarkan...' : 'Daftar'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Sudah punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/login')}
            >
              Masuk sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.3 Auth Guard Component
```typescript
// src/components/auth/AuthGuard.tsx
'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'
import { Loading } from '@/components/common/Loading'

interface AuthGuardProps {
  children: React.ReactNode
  requireAuth?: boolean
  requiredRole?: string[]
}

export default function AuthGuard({ 
  children, 
  requireAuth = true, 
  requiredRole 
}: AuthGuardProps) {
  const { isAuthenticated, user, isLoading } = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (!isLoading) {
      if (requireAuth && !isAuthenticated) {
        router.push('/login')
        return
      }

      if (requiredRole && user && !requiredRole.includes(user.role)) {
        router.push('/unauthorized')
        return
      }
    }
  }, [isAuthenticated, user, isLoading, requireAuth, requiredRole, router])

  if (isLoading) {
    return <Loading />
  }

  if (requireAuth && !isAuthenticated) {
    return null
  }

  if (requiredRole && user && !requiredRole.includes(user.role)) {
    return null
  }

  return <>{children}</>
}
```

## 9. Dashboard Components

### 9.1 Dashboard Layout
```typescript
// src/components/dashboard/Sidebar.tsx
'use client'

import { useState } from 'react'
import { usePathname } from 'next/navigation'
import Link from 'next/link'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/hooks/useAuth'
import {
  HomeIcon,
  CreditCardIcon,
  ArrowsRightLeftIcon,
  ChartBarIcon,
  UserIcon,
  Bars3Icon,
  XMarkIcon,
} from '@heroicons/react/24/outline'

const navigation = [
  { name: 'Dashboard', href: '/dashboard', icon: HomeIcon },
  { name: 'Rekening', href: '/dashboard/accounts', icon: CreditCardIcon },
  { name: 'Transaksi', href: '/dashboard/transactions', icon: ArrowsRightLeftIcon },
  { name: 'Laporan', href: '/dashboard/reports', icon: ChartBarIcon },
  { name: 'Profile', href: '/dashboard/profile', icon: UserIcon },
]

export default function Sidebar() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const pathname = usePathname()
  const { user, logout } = useAuth()

  return (
    <>
      {/* Mobile sidebar */}
      <div className={cn(
        'fixed inset-0 flex z-40 md:hidden',
        sidebarOpen ? 'block' : 'hidden'
      )}>
        <div className="fixed inset-0 bg-gray-600 bg-opacity-75" onClick={() => setSidebarOpen(false)} />
        <div className="relative flex-1 flex flex-col max-w-xs w-full bg-white">
          <div className="absolute top-0 right-0 -mr-12 pt-2">
            <Button
              variant="ghost"
              onClick={() => setSidebarOpen(false)}
              className="ml-1 flex items-center justify-center h-10 w-10 rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
            >
              <XMarkIcon className="h-6 w-6 text-white" />
            </Button>
          </div>
          <SidebarContent />
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className="hidden md:flex md:w-64 md:flex-col md:fixed md:inset-y-0">
        <div className="flex-1 flex flex-col min-h-0 border-r border-gray-200 bg-white">
          <SidebarContent />
        </div>
      </div>

      {/* Mobile menu button */}
      <div className="sticky top-0 z-10 md:hidden pl-1 pt-1 sm:pl-3 sm:pt-3 bg-white border-b">
        <Button
          variant="ghost"
          onClick={() => setSidebarOpen(true)}
          className="-ml-0.5 -mt-0.5 h-12 w-12 inline-flex items-center justify-center"
        >
          <Bars3Icon className="h-6 w-6" />
        </Button>
      </div>
    </>
  )

  function SidebarContent() {
    return (
      <>
        <div className="flex-1 flex flex-col pt-5 pb-4 overflow-y-auto">
          <div className="flex items-center flex-shrink-0 px-4">
            <h2 className="text-lg font-semibold text-emerald-800">
              Bank Syariah Aisyah
            </h2>
          </div>
          <nav className="mt-5 flex-1 px-2 space-y-1">
            {navigation.map((item) => {
              const isActive = pathname === item.href
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={cn(
                    'group flex items-center px-2 py-2 text-sm font-medium rounded-md',
                    isActive
                      ? 'bg-emerald-100 text-emerald-900'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  )}
                >
                  <item.icon
                    className={cn(
                      'mr-3 flex-shrink-0 h-6 w-6',
                      isActive ? 'text-emerald-500' : 'text-gray-400 group-hover:text-gray-500'
                    )}
                  />
                  {item.name}
                </Link>
              )
            })}
          </nav>
        </div>
        
        {/* User info and logout */}
        <div className="flex-shrink-0 flex border-t border-gray-200 p-4">
          <div className="flex-shrink-0 w-full group block">
            <div className="flex items-center">
              <div>
                <div className="inline-flex items-center justify-center h-9 w-9 rounded-full bg-emerald-500">
                  <span className="text-sm font-medium leading-none text-white">
                    {user?.fullName?.charAt(0).toUpperCase()}
                  </span>
                </div>
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-700">{user?.fullName}</p>
                <Button
                  variant="link"
                  onClick={logout}
                  className="text-xs text-gray-500 p-0 h-auto"
                >
                  Keluar
                </Button>
              </div>
            </div>
          </div>
        </div>
      </>
    )
  }
}
```

### 9.2 Dashboard Header
```typescript
// src/components/dashboard/Header.tsx
'use client'

import { Bell, Search } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

interface HeaderProps {
  title: string
  description?: string
}

export default function Header({ title, description }: HeaderProps) {
  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center py-6">
          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold leading-7 text-gray-900 sm:truncate">
              {title}
            </h1>
            {description && (
              <p className="mt-1 text-sm text-gray-500">
                {description}
              </p>
            )}
          </div>
          
          <div className="flex items-center space-x-4">
            <div className="flex-1 max-w-lg">
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Search className="h-5 w-5 text-gray-400" />
                </div>
                <Input
                  type="search"
                  placeholder="Cari..."
                  className="pl-10 pr-4 py-2 w-full"
                />
              </div>
            </div>
            
            <Button variant="ghost" size="icon">
              <Bell className="h-5 w-5" />
            </Button>
          </div>
        </div>
      </div>
    </header>
  )
}
```

### 9.3 Statistics Cards
```typescript
// src/components/dashboard/StatCards.tsx
'use client'

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { formatCurrency } from '@/utils/formatters'
import { 
  CreditCardIcon, 
  ArrowUpIcon, 
  ArrowDownIcon,
  BanknotesIcon 
} from '@heroicons/react/24/outline'

interface StatCard {
  title: string
  value: string
  change?: string
  changeType?: 'increase' | 'decrease'
  icon: React.ComponentType<any>
}

interface StatCardsProps {
  totalBalance: number
  totalAccounts: number
  monthlyIncome: number
  monthlyExpense: number
}

export default function StatCards({ 
  totalBalance, 
  totalAccounts, 
  monthlyIncome, 
  monthlyExpense 
}: StatCardsProps) {
  const stats: StatCard[] = [
    {
      title: 'Total Saldo',
      value: formatCurrency(totalBalance),
      icon: BanknotesIcon,
    },
    {
      title: 'Jumlah Rekening',
      value: totalAccounts.toString(),
      icon: CreditCardIcon,
    },
    {
      title: 'Pemasukan Bulan Ini',
      value: formatCurrency(monthlyIncome),
      change: '+12%',
      changeType: 'increase',
      icon: ArrowUpIcon,
    },
    {
      title: 'Pengeluaran Bulan Ini',
      value: formatCurrency(monthlyExpense),
      change: '-5%',
      changeType: 'decrease',
      icon: ArrowDownIcon,
    },
  ]

  return (
    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
      {stats.map((stat) => (
        <Card key={stat.title}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {stat.title}
            </CardTitle>
            <stat.icon className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stat.value}</div>
            {stat.change && (
              <p className={`text-xs ${
                stat.changeType === 'increase' ? 'text-green-600' : 'text-red-600'
              }`}>
                {stat.change} dari bulan lalu
              </p>
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
```

### 9.4 Recent Transactions Component
```typescript
// src/components/dashboard/RecentTransactions.tsx
'use client'

import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Transaction } from '@/types/transaction'
import { apiService } from '@/lib/api'
import { formatCurrency, formatDate } from '@/utils/formatters'
import { ArrowUpIcon, ArrowDownIcon } from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function RecentTransactions() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchRecentTransactions = async () => {
      try {
        const response = await apiService.get<Transaction[]>('/transactions/recent?limit=5')
        setTransactions(response)
      } catch (error) {
        console.error('Failed to fetch recent transactions:', error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchRecentTransactions()
  }, [])

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Transaksi Terbaru</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="flex items-center space-x-4">
                  <div className="w-10 h-10 bg-gray-200 rounded-full"></div>
                  <div className="flex-1 space-y-2">
                    <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                    <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                  </div>
                  <div className="h-4 bg-gray-200 rounded w-20"></div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <div>
          <CardTitle>Transaksi Terbaru</CardTitle>
          <CardDescription>
            {transactions.length} transaksi terakhir
          </CardDescription>
        </div>
        <Button asChild variant="outline" size="sm">
          <Link href="/dashboard/transactions">
            Lihat Semua
          </Link>
        </Button>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {transactions.map((transaction) => (
            <div key={transaction.id} className="flex items-center space-x-4">
              <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
                transaction.transactionType === 'CREDIT' 
                  ? 'bg-green-100' 
                  : 'bg-red-100'
              }`}>
                {transaction.transactionType === 'CREDIT' ? (
                  <ArrowUpIcon className="w-5 h-5 text-green-600" />
                ) : (
                  <ArrowDownIcon className="w-5 h-5 text-red-600" />
                )}
              </div>
              
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 truncate">
                  {transaction.description}
                </p>
                <p className="text-sm text-gray-500">
                  {formatDate(transaction.transactionDate)} • {transaction.accountNumber}
                </p>
              </div>
              
              <div className="text-right">
                <p className={`text-sm font-medium ${
                  transaction.transactionType === 'CREDIT' 
                    ? 'text-green-600' 
                    : 'text-red-600'
                }`}>
                  {transaction.transactionType === 'CREDIT' ? '+' : '-'}
                  {formatCurrency(transaction.amount)}
                </p>
                <p className="text-xs text-gray-500">
                  {transaction.status}
                </p>
              </div>
            </div>
          ))}
          
          {transactions.length === 0 && (
            <div className="text-center py-6">
              <p className="text-gray-500">Belum ada transaksi</p>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
```

## 10. Account Management Components

### 10.1 Account List
```typescript
// src/components/accounts/AccountList.tsx
'use client'

import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Account } from '@/types/account'
import { formatCurrency } from '@/utils/formatters'
import { useAccounts } from '@/hooks/useAccounts'
import { 
  CreditCardIcon, 
  EyeIcon, 
  PlusIcon 
} from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function AccountList() {
  const { accounts, isLoading } = useAccounts()
  const [visibleBalances, setVisibleBalances] = useState<Record<number, boolean>>({})

  const toggleBalanceVisibility = (accountId: number) => {
    setVisibleBalances(prev => ({
      ...prev,
      [accountId]: !prev[accountId]
    }))
  }

  const getStatusBadge = (status: string) => {
    const statusConfig = {
      ACTIVE: { label: 'Aktif', variant: 'default' as const },
      SUSPENDED: { label: 'Ditangguhkan', variant: 'secondary' as const },
      CLOSED: { label: 'Ditutup', variant: 'destructive' as const },
      FROZEN: { label: 'Dibekukan', variant: 'outline' as const },
    }
    
    return statusConfig[status as keyof typeof statusConfig] || {
      label: status,
      variant: 'outline' as const
    }
  }

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Array.from({ length: 3 }).map((_, i) => (
          <Card key={i} className="animate-pulse">
            <CardHeader>
              <div className="h-4 bg-gray-200 rounded w-3/4"></div>
              <div className="h-3 bg-gray-200 rounded w-1/2"></div>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="h-6 bg-gray-200 rounded w-full"></div>
                <div className="h-4 bg-gray-200 rounded w-2/3"></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold">Rekening Saya</h2>
        <Button asChild>
          <Link href="/dashboard/accounts/create">
            <PlusIcon className="w-4 h-4 mr-2" />
            Buka Rekening Baru
          </Link>
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {accounts.map((account) => {
          const statusBadge = getStatusBadge(account.status)
          const isBalanceVisible = visibleBalances[account.id]
          
          return (
            <Card key={account.id} className="hover:shadow-md transition-shadow">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="p-2 bg-emerald-100 rounded-lg">
                      <CreditCardIcon className="w-5 h-5 text-emerald-600" />
                    </div>
                    <div>
                      <CardTitle className="text-sm font-medium">
                        {account.accountName}
                      </CardTitle>
                      <p className="text-xs text-gray-500">
                        {account.accountNumber}
                      </p>
                    </div>
                  </div>
                  <Badge variant={statusBadge.variant}>
                    {statusBadge.label}
                  </Badge>
                </div>
              </CardHeader>
              
              <CardContent className="pt-0">
                <div className="space-y-3">
                  <div>
                    <p className="text-xs text-gray-500 mb-1">Saldo</p>
                    <div className="flex items-center justify-between">
                      <p className="text-lg font-bold">
                        {isBalanceVisible 
                          ? formatCurrency(account.balance)
                          : '••••••••'
                        }
                      </p>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => toggleBalanceVisibility(account.id)}
                      >
                        <EyeIcon className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  
                  <div className="flex justify-between text-xs text-gray-500">
                    <span>Jenis: {account.accountTypeName}</span>
                    <span>{account.currency}</span>
                  </div>
                  
                  <div className="flex space-x-2">
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/accounts/${account.id}`}>
                        Detail
                      </Link>
                    </Button>
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/transactions?account=${account.accountNumber}`}>
                        Transaksi
                      </Link>
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
        
        {accounts.length === 0 && (
          <Card className="col-span-full">
            <CardContent className="text-center py-12">
              <CreditCardIcon className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                Belum ada rekening
              </h3>
              <p className="text-gray-500 mb-6">
                Buka rekening pertama Anda untuk mulai bertransaksi
              </p>
              <Button asChild>
                <Link href="/dashboard/accounts/create">
                  Buka Rekening Sekarang
                </Link>
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
```

## 11. Transaction Components

### 11.1 Transfer Form
```typescript
// src/components/transactions/TransferForm.tsx
'use client'

import { useState } from 'react'
import { useForm# Frontend Guide - Bank Syariah Aisyah Application (Next.js)

## 1. Project Setup

### 1.1 Initialize Next.js Project
```bash
npx create-next-app@latest bank-syariah-aisyah-frontend --typescript --tailwind --eslint --app
cd bank-syariah-aisyah-frontend
```

### 1.2 Package.json Configuration
```json
{
  "name": "bank-syariah-aisyah-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "type-check": "tsc --noEmit",
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage"
  },
  "dependencies": {
    "@headlessui/react": "^1.7.17",
    "@heroicons/react": "^2.0.18",
    "@hookform/resolvers": "^3.3.2",
    "@radix-ui/react-avatar": "^1.0.4",
    "@radix-ui/react-dialog": "^1.0.5",
    "@radix-ui/react-dropdown-menu": "^2.0.6",
    "@radix-ui/react-label": "^2.0.2",
    "@radix-ui/react-popover": "^1.0.7",
    "@radix-ui/react-select": "^2.0.0",
    "@radix-ui/react-slot": "^1.0.2",
    "@radix-ui/react-tabs": "^1.0.4",
    "@radix-ui/react-toast": "^1.1.5",
    "@tanstack/react-query": "^5.8.4",
    "@types/node": "^20",
    "@types/react": "^18",
    "@types/react-dom": "^18",
    "autoprefixer": "^10",
    "axios": "^1.6.2",
    "class-variance-authority": "^0.7.0",
    "clsx": "^2.0.0",
    "date-fns": "^2.30.0",
    "framer-motion": "^10.16.5",
    "js-cookie": "^3.0.5",
    "lucide-react": "^0.294.0",
    "next": "14.0.3",
    "next-auth": "^4.24.5",
    "next-themes": "^0.2.1",
    "react": "^18",
    "react-dom": "^18",
    "react-hook-form": "^7.48.2",
    "recharts": "^2.8.0",
    "tailwind-merge": "^2.0.0",
    "tailwindcss-animate": "^1.0.7",
    "typescript": "^5",
    "zod": "^3.22.4",
    "zustand": "^4.4.7"
  },
  "devDependencies": {
    "@types/js-cookie": "^3.0.6",
    "@types/jest": "^29.5.8",
    "eslint": "^8",
    "eslint-config-next": "14.0.3",
    "jest": "^29.7.0",
    "jest-environment-jsdom": "^29.7.0",
    "postcss": "^8",
    "tailwindcss": "^3.3.6"
  }
}
```

### 1.3 Next.js Configuration
```typescript
// next.config.js
/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    appDir: true,
  },
  images: {
    domains: ['localhost', 'api.aisyahbank.com'],
  },
  env: {
    NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL,
    NEXT_PUBLIC_APP_NAME: 'Bank Syariah Aisyah',
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${process.env.NEXT_PUBLIC_API_BASE_URL}/:path*`,
      },
    ]
  },
}

module.exports = nextConfig
```

### 1.4 Environment Variables
```bash
# .env.local
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_APP_NAME=Bank Syariah Aisyah
NEXTAUTH_SECRET=your-nextauth-secret
NEXTAUTH_URL=http://localhost:3000
```

## 2. Project Structure

```
src/
├── app/
│   ├── (auth)/
│   │   ├── login/
│   │   │   └── page.tsx
│   │   ├── register/
│   │   │   └── page.tsx
│   │   └── layout.tsx
│   ├── (dashboard)/
│   │   ├── accounts/
│   │   │   ├── page.tsx
│   │   │   ├── create/
│   │   │   │   └── page.tsx
│   │   │   └── [id]/
│   │   │       └── page.tsx
│   │   ├── transactions/
│   │   │   ├── page.tsx
│   │   │   ├── transfer/
│   │   │   │   └── page.tsx
│   │   │   ├── deposit/
│   │   │   │   └── page.tsx
│   │   │   └── withdrawal/
│   │   │       └── page.tsx
│   │   ├── reports/
│   │   │   └── page.tsx
│   │   ├── profile/
│   │   │   └── page.tsx
│   │   ├── page.tsx
│   │   └── layout.tsx
│   ├── globals.css
│   ├── layout.tsx
│   ├── loading.tsx
│   ├── error.tsx
│   └── not-found.tsx
├── components/
│   ├── ui/
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── dialog.tsx
│   │   ├── form.tsx
│   │   ├── input.tsx
│   │   ├── label.tsx
│   │   ├── select.tsx
│   │   ├── table.tsx
│   │   ├── tabs.tsx
│   │   └── toast.tsx
│   ├── auth/
│   │   ├── LoginForm.tsx
│   │   ├── RegisterForm.tsx
│   │   └── AuthGuard.tsx
│   ├── dashboard/
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   ├── StatCards.tsx
│   │   └── RecentTransactions.tsx
│   ├── accounts/
│   │   ├── AccountCard.tsx
│   │   ├── AccountList.tsx
│   │   └── CreateAccountForm.tsx
│   ├── transactions/
│   │   ├── TransactionList.tsx
│   │   ├── TransactionForm.tsx
│   │   ├── TransferForm.tsx
│   │   ├── DepositForm.tsx
│   │   └── WithdrawalForm.tsx
│   └── common/
│       ├── Loading.tsx
│       ├── ErrorBoundary.tsx
│       └── Layout.tsx
├── lib/
│   ├── api.ts
│   ├── auth.ts
│   ├── utils.ts
│   ├── validations.ts
│   └── constants.ts
├── hooks/
│   ├── useAuth.ts
│   ├── useAccounts.ts
│   ├── useTransactions.ts
│   ├── useLocalStorage.ts
│   └── useDebounce.ts
├── store/
│   ├── authStore.ts
│   ├── accountStore.ts
│   └── transactionStore.ts
├── types/
│   ├── auth.ts
│   ├── account.ts
│   ├── transaction.ts
│   └── api.ts
└── utils/
    ├── formatters.ts
    ├── validators.ts
    └── helpers.ts
```

## 3. UI Components with shadcn/ui

### 3.1 Install shadcn/ui
```bash
npx shadcn-ui@latest init
npx shadcn-ui@latest add button card dialog form input label select table tabs toast
```

### 3.2 Button Component
```typescript
// src/components/ui/button.tsx
import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "@/lib/utils"

const buttonVariants = cva(
  "inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
        outline: "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
        secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline",
        islamic: "bg-emerald-600 text-white hover:bg-emerald-700",
      },
      size: {
        default: "h-10 px-4 py-2",
        sm: "h-9 rounded-md px-3",
        lg: "h-11 rounded-md px-8",
        icon: "h-10 w-10",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button"
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    )
  }
)
Button.displayName = "Button"

export { Button, buttonVariants }
```

### 3.3 Card Component
```typescript
// src/components/ui/card.tsx
import * as React from "react"
import { cn } from "@/lib/utils"

const Card = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn(
      "rounded-lg border bg-card text-card-foreground shadow-sm",
      className
    )}
    {...props}
  />
))
Card.displayName = "Card"

const CardHeader = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex flex-col space-y-1.5 p-6", className)}
    {...props}
  />
))
CardHeader.displayName = "CardHeader"

const CardTitle = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLHeadingElement>
>(({ className, ...props }, ref) => (
  <h3
    ref={ref}
    className={cn(
      "text-2xl font-semibold leading-none tracking-tight",
      className
    )}
    {...props}
  />
))
CardTitle.displayName = "CardTitle"

const CardDescription = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => (
  <p
    ref={ref}
    className={cn("text-sm text-muted-foreground", className)}
    {...props}
  />
))
CardDescription.displayName = "CardDescription"

const CardContent = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div ref={ref} className={cn("p-6 pt-0", className)} {...props} />
))
CardContent.displayName = "CardContent"

const CardFooter = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex items-center p-6 pt-0", className)}
    {...props}
  />
))
CardFooter.displayName = "CardFooter"

export { Card, CardHeader, CardFooter, CardTitle, CardDescription, CardContent }
```

## 4. Type Definitions

### 4.1 Authentication Types
```typescript
// src/types/auth.ts
export interface User {
  id: number
  username: string
  email: string
  fullName: string
  role: 'ADMIN' | 'MANAGER' | 'TELLER' | 'CUSTOMER'
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
  emailVerified: boolean
  phoneVerified: boolean
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  fullName: string
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface AuthState {
  user: User | null
  token: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoading: boolean
}
```

### 4.2 Account Types
```typescript
// src/types/account.ts
export interface Account {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  accountTypeCode: string
  balance: number
  availableBalance: number
  currency: string
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED' | 'FROZEN'
  openedDate: string
  closedDate?: string
  customerNumber: string
  customerName: string
  profitSharingPercentage?: number
  isDormant: boolean
  lastTransactionDate?: string
}

export interface AccountType {
  id: number
  typeCode: string
  typeName: string
  description: string
  minimumBalance: number
  maintenanceFee: number
  profitSharingRatio?: number
  isSavings: boolean
  isActive: boolean
}

export interface CreateAccountRequest {
  accountTypeId: number
  accountName: string
  initialDeposit: number
  purpose?: string
}

export interface AccountResponse {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  balance: number
  status: string
  openedDate: string
}
```

### 4.3 Transaction Types
```typescript
// src/types/transaction.ts
export interface Transaction {
  id: number
  transactionNumber: string
  accountNumber: string
  accountName: string
  categoryName: string
  amount: number
  transactionType: 'CREDIT' | 'DEBIT'
  description: string
  balanceBefore: number
  balanceAfter: number
  transactionDate: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REVERSED'
  channel: 'TELLER' | 'ATM' | 'MOBILE' | 'WEB'
  toAccountNumber?: string
  toAccountName?: string
}

export interface DepositRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface WithdrawalRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface TransferRequest {
  fromAccountNumber: string
  toAccountNumber: string
  amount: number
  description: string
}

export interface TransactionResponse {
  id: number
  transactionNumber: string
  amount: number
  description: string
  transactionDate: string
  status: string
  balanceAfter: number
}
```

## 5. API Service Layer

### 5.1 API Configuration
```typescript
// src/lib/api.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { getToken, removeTokens } from './auth'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1'

class ApiService {
  private api: AxiosInstance

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 10000,
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    // Request interceptor to add auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = getToken()
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Response interceptor to handle auth errors
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          removeTokens()
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }
    )
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.get(url, config)
    return response.data
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.post(url, data, config)
    return response.data
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.put(url, data, config)
    return response.data
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.delete(url, config)
    return response.data
  }
}

export const apiService = new ApiService()
```

### 5.2 Authentication Service
```typescript
// src/lib/auth.ts
import { apiService } from './api'
import { LoginRequest, RegisterRequest, LoginResponse } from '@/types/auth'
import Cookies from 'js-cookie'

const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEY = 'user'

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiService.post<LoginResponse>('/auth/login', credentials)
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  async register(userData: RegisterRequest): Promise<void> {
    await apiService.post('/auth/register', userData)
  },

  async refreshToken(): Promise<LoginResponse> {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      throw new Error('No refresh token available')
    }
    
    const response = await apiService.post<LoginResponse>('/auth/refresh', { refreshToken })
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  logout() {
    removeTokens()
    removeUser()
  }
}

// Token management functions
export const setTokens = (token: string, refreshToken: string) => {
  Cookies.set(TOKEN_KEY, token, { expires: 1 }) // 1 day
  Cookies.set(REFRESH_TOKEN_KEY, refreshToken, { expires: 7 }) // 7 days
}

export const getToken = (): string | undefined => {
  return Cookies.get(TOKEN_KEY)
}

export const getRefreshToken = (): string | undefined => {
  return Cookies.get(REFRESH_TOKEN_KEY)
}

export const removeTokens = () => {
  Cookies.remove(TOKEN_KEY)
  Cookies.remove(REFRESH_TOKEN_KEY)
}

export const setUser = (user: any) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const getUser = () => {
  if (typeof window === 'undefined') return null
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}

export const removeUser = () => {
  localStorage.removeItem(USER_KEY)
}

export const isAuthenticated = (): boolean => {
  return !!getToken()
}
```

## 6. State Management with Zustand

### 6.1 Auth Store
```typescript
// src/store/authStore.ts
import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'
import { User, LoginRequest, RegisterRequest, AuthState } from '@/types/auth'
import { authService } from '@/lib/auth'

interface AuthStore extends AuthState {
  login: (credentials: LoginRequest) => Promise<void>
  register: (userData: RegisterRequest) => Promise<void>
  logout: () => void
  refreshToken: () => Promise<void>
  setUser: (user: User) => void
  setToken: (token: string, refreshToken: string) => void
  clearAuth: () => void
}

export const useAuthStore = create<AuthStore>()(
  devtools(
    persist(
      (set, get) => ({
        user: null,
        token: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,

        login: async (credentials: LoginRequest) => {
          set({ isLoading: true })
          try {
            const response = await authService.login(credentials)
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
              isLoading: false,
            })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        register: async (userData: RegisterRequest) => {
          set({ isLoading: true })
          try {
            await authService.register(userData)
            set({ isLoading: false })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        logout: () => {
          authService.logout()
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },

        refreshToken: async () => {
          try {
            const response = await authService.refreshToken()
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
            })
          } catch (error) {
            get().logout()
            throw error
          }
        },

        setUser: (user: User) => {
          set({ user })
        },

        setToken: (token: string, refreshToken: string) => {
          set({ token, refreshToken, isAuthenticated: true })
        },

        clearAuth: () => {
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },
      }),
      {
        name: 'auth-storage',
        partialize: (state) => ({
          user: state.user,
          token: state.token,
          refreshToken: state.refreshToken,
          isAuthenticated: state.isAuthenticated,
        }),
      }
    )
  )
)
```

### 6.2 Account Store
```typescript
// src/store/accountStore.ts
import { create } from 'zustand'
import { devtools } from 'zustand/middleware'
import { Account, AccountType, CreateAccountRequest } from '@/types/account'
import { apiService } from '@/lib/api'

interface AccountState {
  accounts: Account[]
  accountTypes: AccountType[]
  selectedAccount: Account | null
  isLoading: boolean
  error: string | null
}

interface AccountActions {
  fetchAccounts: () => Promise<void>
  fetchAccountTypes: () => Promise<void>
  createAccount: (request: CreateAccountRequest) => Promise<void>
  getAccountById: (id: number) => Promise<void>
  getAccountByNumber: (accountNumber: string) => Promise<Account>
  setSelectedAccount: (account: Account | null) => void
  clearError: () => void
}

type AccountStore = AccountState & AccountActions

export const useAccountStore = create<AccountStore>()(
  devtools(
    (set, get) => ({
      accounts: [],
      accountTypes: [],
      selectedAccount: null,
      isLoading: false,
      error: null,

      fetchAccounts: async () => {
        set({ isLoading: true, error: null })
        try {
          const accounts = await apiService.get<Account[]>('/accounts/my-accounts')
          set({ accounts, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch accounts',
            isLoading: false 
          })
        }
      },

      fetchAccountTypes: async () => {
        try {
          const accountTypes = await apiService.get<AccountType[]>('/account-types')
          set({ accountTypes })
        } catch (error: any) {
          set({ error: error.response?.data?.message || 'Failed to fetch account types' })
        }
      },

      createAccount: async (request: CreateAccountRequest) => {
        set({ isLoading: true, error: null })
        try {
          const newAccount = await apiService.post<Account>('/accounts', request)
          set(state => ({ 
            accounts: [...state.accounts, newAccount],
            isLoading: false 
          }))
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to create account',
            isLoading: false 
          })
          throw error
        }
      },

      getAccountById: async (id: number) => {
        set({ isLoading: true, error: null })
        try {
          const account = await apiService.get<Account>(`/accounts/${id}`)
          set({ selectedAccount: account, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch account',
            isLoading: false 
          })
        }
      },

      getAccountByNumber: async (accountNumber: string) => {
        const response = await apiService.get<Account>(`/accounts/number/${accountNumber}`)
        return response
      },

      setSelectedAccount: (account: Account | null) => {
        set({ selectedAccount: account })
      },

      clearError: () => {
        set({ error: null })
      },
    })
  )
)
```

## 7. Custom Hooks

### 7.1 useAuth Hook
```typescript
// src/hooks/useAuth.ts
import { useAuthStore } from '@/store/authStore'
import { useRouter } from 'next/navigation'
import { useEffect } from 'react'

export const useAuth = (requireAuth = true) => {
  const authStore = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (requireAuth && !authStore.isAuthenticated) {
      router.push('/login')
    }
  }, [authStore.isAuthenticated, requireAuth, router])

  return {
    user: authStore.user,
    isAuthenticated: authStore.isAuthenticated,
    isLoading: authStore.isLoading,
    login: authStore.login,
    register: authStore.register,
    logout: authStore.logout,
    refreshToken: authStore.refreshToken,
  }
}
```

### 7.2 useAccounts Hook
```typescript
// src/hooks/useAccounts.ts
import { useAccountStore } from '@/store/accountStore'
import { useEffect } from 'react'

export const useAccounts = () => {
  const accountStore = useAccountStore()

  useEffect(() => {
    accountStore.fetchAccounts()
    accountStore.fetchAccountTypes()
  }, [])

  return {
    accounts: accountStore.accounts,
    accountTypes: accountStore.accountTypes,
    selectedAccount: accountStore.selectedAccount,
    isLoading: accountStore.isLoading,
    error: accountStore.error,
    createAccount: accountStore.createAccount,
    getAccountById: accountStore.getAccountById,
    getAccountByNumber: accountStore.getAccountByNumber,
    setSelectedAccount: accountStore.setSelectedAccount,
    clearError: accountStore.clearError,
  }
}
```

### 7.3 useTransactions Hook
```typescript
// src/hooks/useTransactions.ts
import { useState, useEffect } from 'react'
import { Transaction, DepositRequest, WithdrawalRequest, TransferRequest } from '@/types/transaction'
import { apiService } from '@/lib/api'

export const useTransactions = (accountId?: number) => {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchTransactions = async (accountId?: number) => {
    setIsLoading(true)
    setError(null)
    try {
      const url = accountId ? `/transactions/account/${accountId}` : '/transactions/my-transactions'
      const response = await apiService.get<Transaction[]>(url)
      setTransactions(response)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch transactions')
    } finally {
      setIsLoading(false)
    }
  }

  const deposit = async (request: DepositRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/deposit', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process deposit')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const withdraw = async (request: WithdrawalRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/withdrawal', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process withdrawal')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const transfer = async (request: TransferRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/transfer', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process transfer')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchTransactions(accountId)
  }, [accountId])

  return {
    transactions,
    isLoading,
    error,
    fetchTransactions,
    deposit,
    withdraw,
    transfer,
  }
}
```

## 8. Authentication Components

### 8.1 Login Form
```typescript
// src/components/auth/LoginForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { LoginRequest } from '@/types/auth'
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/24/outline'

const loginSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

export default function LoginForm() {
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const { login } = useAuth(false)
  const router = useRouter()

  const form = useForm<LoginRequest>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  })

  const onSubmit = async (data: LoginRequest) => {
    setIsLoading(true)
    try {
      await login(data)
      router.push('/dashboard')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Login failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Bank Syariah Aisyah
          </CardTitle>
          <CardDescription>
            Masuk ke akun perbankan syariah Anda
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Username</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Masukkan username"
                        {...field}
                        disabled={isLoading}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password</FormLabel>
                    <FormControl>
                      <div className="relative">
                        <Input
                          type={showPassword ? 'text' : 'password'}
                          placeholder="Masukkan password"
                          {...field}
                          disabled={isLoading}
                        />
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                          onClick={() => setShowPassword(!showPassword)}
                          disabled={isLoading}
                        >
                          {showPassword ? (
                            <EyeSlashIcon className="h-4 w-4" />
                          ) : (
                            <EyeIcon className="h-4 w-4" />
                          )}
                        </Button>
                      </div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Memproses...' : 'Masuk'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Belum punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/register')}
            >
              Daftar sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.2 Register Form
```typescript
// src/components/auth/RegisterForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { RegisterRequest } from '@/types/auth'

const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    : '<rootDir>/pages/$1',
    '^@/lib/(.*)const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    .min(8, 'Password minimal 8 karakter')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/, 
      'Password harus mengandung huruf besar, kecil, angka, dan karakter khusus'),
  fullName: z.string().min(2, 'Nama lengkap minimal 2 karakter'),
  phoneNumber: z.string().regex(/^(\+62|62|0)8[1-9][0-9]{6,9}$/, 'Format nomor HP tidak valid').optional(),
  dateOfBirth: z.string().optional(),
  nationalId: z.string().regex(/^[0-9]{16}$/, 'NIK harus 16 digit angka').optional(),
  address: z.string().max(500, 'Alamat maksimal 500 karakter').optional(),
})

export default function RegisterForm() {
  const [isLoading, setIsLoading] = useState(false)
  const { register } = useAuth(false)
  const router = useRouter()

  const form = useForm<RegisterRequest>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      fullName: '',
      phoneNumber: '',
      dateOfBirth: '',
      nationalId: '',
      address: '',
    },
  })

  const onSubmit = async (data: RegisterRequest) => {
    setIsLoading(true)
    try {
      await register(data)
      router.push('/login?message=Registration successful')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Registration failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100 py-8">
      <Card className="w-full max-w-2xl">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Daftar Akun Baru
          </CardTitle>
          <CardDescription>
            Bergabunglah dengan Bank Syariah Aisyah
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="username"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Username *</FormLabel>
                      <FormControl>
                        <Input placeholder="Username" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="email"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Email *</FormLabel>
                      <FormControl>
                        <Input type="email" placeholder="Email" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password *</FormLabel>
                    <FormControl>
                      <Input type="password" placeholder="Password" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="fullName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Nama Lengkap *</FormLabel>
                    <FormControl>
                      <Input placeholder="Nama lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="phoneNumber"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Nomor HP</FormLabel>
                      <FormControl>
                        <Input placeholder="08xxxxxxxxx" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="dateOfBirth"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Tanggal Lahir</FormLabel>
                      <FormControl>
                        <Input type="date" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="nationalId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>NIK (Nomor Induk Kependudukan)</FormLabel>
                    <FormControl>
                      <Input placeholder="16 digit NIK" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="address"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Alamat</FormLabel>
                    <FormControl>
                      <Input placeholder="Alamat lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Mendaftarkan...' : 'Daftar'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Sudah punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/login')}
            >
              Masuk sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.3 Auth Guard Component
```typescript
// src/components/auth/AuthGuard.tsx
'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'
import { Loading } from '@/components/common/Loading'

interface AuthGuardProps {
  children: React.ReactNode
  requireAuth?: boolean
  requiredRole?: string[]
}

export default function AuthGuard({ 
  children, 
  requireAuth = true, 
  requiredRole 
}: AuthGuardProps) {
  const { isAuthenticated, user, isLoading } = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (!isLoading) {
      if (requireAuth && !isAuthenticated) {
        router.push('/login')
        return
      }

      if (requiredRole && user && !requiredRole.includes(user.role)) {
        router.push('/unauthorized')
        return
      }
    }
  }, [isAuthenticated, user, isLoading, requireAuth, requiredRole, router])

  if (isLoading) {
    return <Loading />
  }

  if (requireAuth && !isAuthenticated) {
    return null
  }

  if (requiredRole && user && !requiredRole.includes(user.role)) {
    return null
  }

  return <>{children}</>
}
```

## 9. Dashboard Components

### 9.1 Dashboard Layout
```typescript
// src/components/dashboard/Sidebar.tsx
'use client'

import { useState } from 'react'
import { usePathname } from 'next/navigation'
import Link from 'next/link'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/hooks/useAuth'
import {
  HomeIcon,
  CreditCardIcon,
  ArrowsRightLeftIcon,
  ChartBarIcon,
  UserIcon,
  Bars3Icon,
  XMarkIcon,
} from '@heroicons/react/24/outline'

const navigation = [
  { name: 'Dashboard', href: '/dashboard', icon: HomeIcon },
  { name: 'Rekening', href: '/dashboard/accounts', icon: CreditCardIcon },
  { name: 'Transaksi', href: '/dashboard/transactions', icon: ArrowsRightLeftIcon },
  { name: 'Laporan', href: '/dashboard/reports', icon: ChartBarIcon },
  { name: 'Profile', href: '/dashboard/profile', icon: UserIcon },
]

export default function Sidebar() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const pathname = usePathname()
  const { user, logout } = useAuth()

  return (
    <>
      {/* Mobile sidebar */}
      <div className={cn(
        'fixed inset-0 flex z-40 md:hidden',
        sidebarOpen ? 'block' : 'hidden'
      )}>
        <div className="fixed inset-0 bg-gray-600 bg-opacity-75" onClick={() => setSidebarOpen(false)} />
        <div className="relative flex-1 flex flex-col max-w-xs w-full bg-white">
          <div className="absolute top-0 right-0 -mr-12 pt-2">
            <Button
              variant="ghost"
              onClick={() => setSidebarOpen(false)}
              className="ml-1 flex items-center justify-center h-10 w-10 rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
            >
              <XMarkIcon className="h-6 w-6 text-white" />
            </Button>
          </div>
          <SidebarContent />
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className="hidden md:flex md:w-64 md:flex-col md:fixed md:inset-y-0">
        <div className="flex-1 flex flex-col min-h-0 border-r border-gray-200 bg-white">
          <SidebarContent />
        </div>
      </div>

      {/* Mobile menu button */}
      <div className="sticky top-0 z-10 md:hidden pl-1 pt-1 sm:pl-3 sm:pt-3 bg-white border-b">
        <Button
          variant="ghost"
          onClick={() => setSidebarOpen(true)}
          className="-ml-0.5 -mt-0.5 h-12 w-12 inline-flex items-center justify-center"
        >
          <Bars3Icon className="h-6 w-6" />
        </Button>
      </div>
    </>
  )

  function SidebarContent() {
    return (
      <>
        <div className="flex-1 flex flex-col pt-5 pb-4 overflow-y-auto">
          <div className="flex items-center flex-shrink-0 px-4">
            <h2 className="text-lg font-semibold text-emerald-800">
              Bank Syariah Aisyah
            </h2>
          </div>
          <nav className="mt-5 flex-1 px-2 space-y-1">
            {navigation.map((item) => {
              const isActive = pathname === item.href
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={cn(
                    'group flex items-center px-2 py-2 text-sm font-medium rounded-md',
                    isActive
                      ? 'bg-emerald-100 text-emerald-900'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  )}
                >
                  <item.icon
                    className={cn(
                      'mr-3 flex-shrink-0 h-6 w-6',
                      isActive ? 'text-emerald-500' : 'text-gray-400 group-hover:text-gray-500'
                    )}
                  />
                  {item.name}
                </Link>
              )
            })}
          </nav>
        </div>
        
        {/* User info and logout */}
        <div className="flex-shrink-0 flex border-t border-gray-200 p-4">
          <div className="flex-shrink-0 w-full group block">
            <div className="flex items-center">
              <div>
                <div className="inline-flex items-center justify-center h-9 w-9 rounded-full bg-emerald-500">
                  <span className="text-sm font-medium leading-none text-white">
                    {user?.fullName?.charAt(0).toUpperCase()}
                  </span>
                </div>
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-700">{user?.fullName}</p>
                <Button
                  variant="link"
                  onClick={logout}
                  className="text-xs text-gray-500 p-0 h-auto"
                >
                  Keluar
                </Button>
              </div>
            </div>
          </div>
        </div>
      </>
    )
  }
}
```

### 9.2 Dashboard Header
```typescript
// src/components/dashboard/Header.tsx
'use client'

import { Bell, Search } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

interface HeaderProps {
  title: string
  description?: string
}

export default function Header({ title, description }: HeaderProps) {
  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center py-6">
          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold leading-7 text-gray-900 sm:truncate">
              {title}
            </h1>
            {description && (
              <p className="mt-1 text-sm text-gray-500">
                {description}
              </p>
            )}
          </div>
          
          <div className="flex items-center space-x-4">
            <div className="flex-1 max-w-lg">
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Search className="h-5 w-5 text-gray-400" />
                </div>
                <Input
                  type="search"
                  placeholder="Cari..."
                  className="pl-10 pr-4 py-2 w-full"
                />
              </div>
            </div>
            
            <Button variant="ghost" size="icon">
              <Bell className="h-5 w-5" />
            </Button>
          </div>
        </div>
      </div>
    </header>
  )
}
```

### 9.3 Statistics Cards
```typescript
// src/components/dashboard/StatCards.tsx
'use client'

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { formatCurrency } from '@/utils/formatters'
import { 
  CreditCardIcon, 
  ArrowUpIcon, 
  ArrowDownIcon,
  BanknotesIcon 
} from '@heroicons/react/24/outline'

interface StatCard {
  title: string
  value: string
  change?: string
  changeType?: 'increase' | 'decrease'
  icon: React.ComponentType<any>
}

interface StatCardsProps {
  totalBalance: number
  totalAccounts: number
  monthlyIncome: number
  monthlyExpense: number
}

export default function StatCards({ 
  totalBalance, 
  totalAccounts, 
  monthlyIncome, 
  monthlyExpense 
}: StatCardsProps) {
  const stats: StatCard[] = [
    {
      title: 'Total Saldo',
      value: formatCurrency(totalBalance),
      icon: BanknotesIcon,
    },
    {
      title: 'Jumlah Rekening',
      value: totalAccounts.toString(),
      icon: CreditCardIcon,
    },
    {
      title: 'Pemasukan Bulan Ini',
      value: formatCurrency(monthlyIncome),
      change: '+12%',
      changeType: 'increase',
      icon: ArrowUpIcon,
    },
    {
      title: 'Pengeluaran Bulan Ini',
      value: formatCurrency(monthlyExpense),
      change: '-5%',
      changeType: 'decrease',
      icon: ArrowDownIcon,
    },
  ]

  return (
    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
      {stats.map((stat) => (
        <Card key={stat.title}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {stat.title}
            </CardTitle>
            <stat.icon className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stat.value}</div>
            {stat.change && (
              <p className={`text-xs ${
                stat.changeType === 'increase' ? 'text-green-600' : 'text-red-600'
              }`}>
                {stat.change} dari bulan lalu
              </p>
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
```

### 9.4 Recent Transactions Component
```typescript
// src/components/dashboard/RecentTransactions.tsx
'use client'

import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Transaction } from '@/types/transaction'
import { apiService } from '@/lib/api'
import { formatCurrency, formatDate } from '@/utils/formatters'
import { ArrowUpIcon, ArrowDownIcon } from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function RecentTransactions() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchRecentTransactions = async () => {
      try {
        const response = await apiService.get<Transaction[]>('/transactions/recent?limit=5')
        setTransactions(response)
      } catch (error) {
        console.error('Failed to fetch recent transactions:', error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchRecentTransactions()
  }, [])

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Transaksi Terbaru</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="flex items-center space-x-4">
                  <div className="w-10 h-10 bg-gray-200 rounded-full"></div>
                  <div className="flex-1 space-y-2">
                    <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                    <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                  </div>
                  <div className="h-4 bg-gray-200 rounded w-20"></div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <div>
          <CardTitle>Transaksi Terbaru</CardTitle>
          <CardDescription>
            {transactions.length} transaksi terakhir
          </CardDescription>
        </div>
        <Button asChild variant="outline" size="sm">
          <Link href="/dashboard/transactions">
            Lihat Semua
          </Link>
        </Button>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {transactions.map((transaction) => (
            <div key={transaction.id} className="flex items-center space-x-4">
              <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
                transaction.transactionType === 'CREDIT' 
                  ? 'bg-green-100' 
                  : 'bg-red-100'
              }`}>
                {transaction.transactionType === 'CREDIT' ? (
                  <ArrowUpIcon className="w-5 h-5 text-green-600" />
                ) : (
                  <ArrowDownIcon className="w-5 h-5 text-red-600" />
                )}
              </div>
              
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 truncate">
                  {transaction.description}
                </p>
                <p className="text-sm text-gray-500">
                  {formatDate(transaction.transactionDate)} • {transaction.accountNumber}
                </p>
              </div>
              
              <div className="text-right">
                <p className={`text-sm font-medium ${
                  transaction.transactionType === 'CREDIT' 
                    ? 'text-green-600' 
                    : 'text-red-600'
                }`}>
                  {transaction.transactionType === 'CREDIT' ? '+' : '-'}
                  {formatCurrency(transaction.amount)}
                </p>
                <p className="text-xs text-gray-500">
                  {transaction.status}
                </p>
              </div>
            </div>
          ))}
          
          {transactions.length === 0 && (
            <div className="text-center py-6">
              <p className="text-gray-500">Belum ada transaksi</p>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
```

## 10. Account Management Components

### 10.1 Account List
```typescript
// src/components/accounts/AccountList.tsx
'use client'

import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Account } from '@/types/account'
import { formatCurrency } from '@/utils/formatters'
import { useAccounts } from '@/hooks/useAccounts'
import { 
  CreditCardIcon, 
  EyeIcon, 
  PlusIcon 
} from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function AccountList() {
  const { accounts, isLoading } = useAccounts()
  const [visibleBalances, setVisibleBalances] = useState<Record<number, boolean>>({})

  const toggleBalanceVisibility = (accountId: number) => {
    setVisibleBalances(prev => ({
      ...prev,
      [accountId]: !prev[accountId]
    }))
  }

  const getStatusBadge = (status: string) => {
    const statusConfig = {
      ACTIVE: { label: 'Aktif', variant: 'default' as const },
      SUSPENDED: { label: 'Ditangguhkan', variant: 'secondary' as const },
      CLOSED: { label: 'Ditutup', variant: 'destructive' as const },
      FROZEN: { label: 'Dibekukan', variant: 'outline' as const },
    }
    
    return statusConfig[status as keyof typeof statusConfig] || {
      label: status,
      variant: 'outline' as const
    }
  }

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Array.from({ length: 3 }).map((_, i) => (
          <Card key={i} className="animate-pulse">
            <CardHeader>
              <div className="h-4 bg-gray-200 rounded w-3/4"></div>
              <div className="h-3 bg-gray-200 rounded w-1/2"></div>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="h-6 bg-gray-200 rounded w-full"></div>
                <div className="h-4 bg-gray-200 rounded w-2/3"></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold">Rekening Saya</h2>
        <Button asChild>
          <Link href="/dashboard/accounts/create">
            <PlusIcon className="w-4 h-4 mr-2" />
            Buka Rekening Baru
          </Link>
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {accounts.map((account) => {
          const statusBadge = getStatusBadge(account.status)
          const isBalanceVisible = visibleBalances[account.id]
          
          return (
            <Card key={account.id} className="hover:shadow-md transition-shadow">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="p-2 bg-emerald-100 rounded-lg">
                      <CreditCardIcon className="w-5 h-5 text-emerald-600" />
                    </div>
                    <div>
                      <CardTitle className="text-sm font-medium">
                        {account.accountName}
                      </CardTitle>
                      <p className="text-xs text-gray-500">
                        {account.accountNumber}
                      </p>
                    </div>
                  </div>
                  <Badge variant={statusBadge.variant}>
                    {statusBadge.label}
                  </Badge>
                </div>
              </CardHeader>
              
              <CardContent className="pt-0">
                <div className="space-y-3">
                  <div>
                    <p className="text-xs text-gray-500 mb-1">Saldo</p>
                    <div className="flex items-center justify-between">
                      <p className="text-lg font-bold">
                        {isBalanceVisible 
                          ? formatCurrency(account.balance)
                          : '••••••••'
                        }
                      </p>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => toggleBalanceVisibility(account.id)}
                      >
                        <EyeIcon className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  
                  <div className="flex justify-between text-xs text-gray-500">
                    <span>Jenis: {account.accountTypeName}</span>
                    <span>{account.currency}</span>
                  </div>
                  
                  <div className="flex space-x-2">
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/accounts/${account.id}`}>
                        Detail
                      </Link>
                    </Button>
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/transactions?account=${account.accountNumber}`}>
                        Transaksi
                      </Link>
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
        
        {accounts.length === 0 && (
          <Card className="col-span-full">
            <CardContent className="text-center py-12">
              <CreditCardIcon className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                Belum ada rekening
              </h3>
              <p className="text-gray-500 mb-6">
                Buka rekening pertama Anda untuk mulai bertransaksi
              </p>
              <Button asChild>
                <Link href="/dashboard/accounts/create">
                  Buka Rekening Sekarang
                </Link>
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
```

## 11. Transaction Components

### 11.1 Transfer Form
```typescript
// src/components/transactions/TransferForm.tsx
'use client'

import { useState } from 'react'
import { useForm# Frontend Guide - Bank Syariah Aisyah Application (Next.js)

## 1. Project Setup

### 1.1 Initialize Next.js Project
```bash
npx create-next-app@latest bank-syariah-aisyah-frontend --typescript --tailwind --eslint --app
cd bank-syariah-aisyah-frontend
```

### 1.2 Package.json Configuration
```json
{
  "name": "bank-syariah-aisyah-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "type-check": "tsc --noEmit",
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage"
  },
  "dependencies": {
    "@headlessui/react": "^1.7.17",
    "@heroicons/react": "^2.0.18",
    "@hookform/resolvers": "^3.3.2",
    "@radix-ui/react-avatar": "^1.0.4",
    "@radix-ui/react-dialog": "^1.0.5",
    "@radix-ui/react-dropdown-menu": "^2.0.6",
    "@radix-ui/react-label": "^2.0.2",
    "@radix-ui/react-popover": "^1.0.7",
    "@radix-ui/react-select": "^2.0.0",
    "@radix-ui/react-slot": "^1.0.2",
    "@radix-ui/react-tabs": "^1.0.4",
    "@radix-ui/react-toast": "^1.1.5",
    "@tanstack/react-query": "^5.8.4",
    "@types/node": "^20",
    "@types/react": "^18",
    "@types/react-dom": "^18",
    "autoprefixer": "^10",
    "axios": "^1.6.2",
    "class-variance-authority": "^0.7.0",
    "clsx": "^2.0.0",
    "date-fns": "^2.30.0",
    "framer-motion": "^10.16.5",
    "js-cookie": "^3.0.5",
    "lucide-react": "^0.294.0",
    "next": "14.0.3",
    "next-auth": "^4.24.5",
    "next-themes": "^0.2.1",
    "react": "^18",
    "react-dom": "^18",
    "react-hook-form": "^7.48.2",
    "recharts": "^2.8.0",
    "tailwind-merge": "^2.0.0",
    "tailwindcss-animate": "^1.0.7",
    "typescript": "^5",
    "zod": "^3.22.4",
    "zustand": "^4.4.7"
  },
  "devDependencies": {
    "@types/js-cookie": "^3.0.6",
    "@types/jest": "^29.5.8",
    "eslint": "^8",
    "eslint-config-next": "14.0.3",
    "jest": "^29.7.0",
    "jest-environment-jsdom": "^29.7.0",
    "postcss": "^8",
    "tailwindcss": "^3.3.6"
  }
}
```

### 1.3 Next.js Configuration
```typescript
// next.config.js
/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    appDir: true,
  },
  images: {
    domains: ['localhost', 'api.aisyahbank.com'],
  },
  env: {
    NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL,
    NEXT_PUBLIC_APP_NAME: 'Bank Syariah Aisyah',
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${process.env.NEXT_PUBLIC_API_BASE_URL}/:path*`,
      },
    ]
  },
}

module.exports = nextConfig
```

### 1.4 Environment Variables
```bash
# .env.local
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_APP_NAME=Bank Syariah Aisyah
NEXTAUTH_SECRET=your-nextauth-secret
NEXTAUTH_URL=http://localhost:3000
```

## 2. Project Structure

```
src/
├── app/
│   ├── (auth)/
│   │   ├── login/
│   │   │   └── page.tsx
│   │   ├── register/
│   │   │   └── page.tsx
│   │   └── layout.tsx
│   ├── (dashboard)/
│   │   ├── accounts/
│   │   │   ├── page.tsx
│   │   │   ├── create/
│   │   │   │   └── page.tsx
│   │   │   └── [id]/
│   │   │       └── page.tsx
│   │   ├── transactions/
│   │   │   ├── page.tsx
│   │   │   ├── transfer/
│   │   │   │   └── page.tsx
│   │   │   ├── deposit/
│   │   │   │   └── page.tsx
│   │   │   └── withdrawal/
│   │   │       └── page.tsx
│   │   ├── reports/
│   │   │   └── page.tsx
│   │   ├── profile/
│   │   │   └── page.tsx
│   │   ├── page.tsx
│   │   └── layout.tsx
│   ├── globals.css
│   ├── layout.tsx
│   ├── loading.tsx
│   ├── error.tsx
│   └── not-found.tsx
├── components/
│   ├── ui/
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── dialog.tsx
│   │   ├── form.tsx
│   │   ├── input.tsx
│   │   ├── label.tsx
│   │   ├── select.tsx
│   │   ├── table.tsx
│   │   ├── tabs.tsx
│   │   └── toast.tsx
│   ├── auth/
│   │   ├── LoginForm.tsx
│   │   ├── RegisterForm.tsx
│   │   └── AuthGuard.tsx
│   ├── dashboard/
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   ├── StatCards.tsx
│   │   └── RecentTransactions.tsx
│   ├── accounts/
│   │   ├── AccountCard.tsx
│   │   ├── AccountList.tsx
│   │   └── CreateAccountForm.tsx
│   ├── transactions/
│   │   ├── TransactionList.tsx
│   │   ├── TransactionForm.tsx
│   │   ├── TransferForm.tsx
│   │   ├── DepositForm.tsx
│   │   └── WithdrawalForm.tsx
│   └── common/
│       ├── Loading.tsx
│       ├── ErrorBoundary.tsx
│       └── Layout.tsx
├── lib/
│   ├── api.ts
│   ├── auth.ts
│   ├── utils.ts
│   ├── validations.ts
│   └── constants.ts
├── hooks/
│   ├── useAuth.ts
│   ├── useAccounts.ts
│   ├── useTransactions.ts
│   ├── useLocalStorage.ts
│   └── useDebounce.ts
├── store/
│   ├── authStore.ts
│   ├── accountStore.ts
│   └── transactionStore.ts
├── types/
│   ├── auth.ts
│   ├── account.ts
│   ├── transaction.ts
│   └── api.ts
└── utils/
    ├── formatters.ts
    ├── validators.ts
    └── helpers.ts
```

## 3. UI Components with shadcn/ui

### 3.1 Install shadcn/ui
```bash
npx shadcn-ui@latest init
npx shadcn-ui@latest add button card dialog form input label select table tabs toast
```

### 3.2 Button Component
```typescript
// src/components/ui/button.tsx
import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "@/lib/utils"

const buttonVariants = cva(
  "inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
        outline: "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
        secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline",
        islamic: "bg-emerald-600 text-white hover:bg-emerald-700",
      },
      size: {
        default: "h-10 px-4 py-2",
        sm: "h-9 rounded-md px-3",
        lg: "h-11 rounded-md px-8",
        icon: "h-10 w-10",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button"
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    )
  }
)
Button.displayName = "Button"

export { Button, buttonVariants }
```

### 3.3 Card Component
```typescript
// src/components/ui/card.tsx
import * as React from "react"
import { cn } from "@/lib/utils"

const Card = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn(
      "rounded-lg border bg-card text-card-foreground shadow-sm",
      className
    )}
    {...props}
  />
))
Card.displayName = "Card"

const CardHeader = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex flex-col space-y-1.5 p-6", className)}
    {...props}
  />
))
CardHeader.displayName = "CardHeader"

const CardTitle = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLHeadingElement>
>(({ className, ...props }, ref) => (
  <h3
    ref={ref}
    className={cn(
      "text-2xl font-semibold leading-none tracking-tight",
      className
    )}
    {...props}
  />
))
CardTitle.displayName = "CardTitle"

const CardDescription = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => (
  <p
    ref={ref}
    className={cn("text-sm text-muted-foreground", className)}
    {...props}
  />
))
CardDescription.displayName = "CardDescription"

const CardContent = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div ref={ref} className={cn("p-6 pt-0", className)} {...props} />
))
CardContent.displayName = "CardContent"

const CardFooter = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex items-center p-6 pt-0", className)}
    {...props}
  />
))
CardFooter.displayName = "CardFooter"

export { Card, CardHeader, CardFooter, CardTitle, CardDescription, CardContent }
```

## 4. Type Definitions

### 4.1 Authentication Types
```typescript
// src/types/auth.ts
export interface User {
  id: number
  username: string
  email: string
  fullName: string
  role: 'ADMIN' | 'MANAGER' | 'TELLER' | 'CUSTOMER'
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
  emailVerified: boolean
  phoneVerified: boolean
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  fullName: string
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface AuthState {
  user: User | null
  token: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoading: boolean
}
```

### 4.2 Account Types
```typescript
// src/types/account.ts
export interface Account {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  accountTypeCode: string
  balance: number
  availableBalance: number
  currency: string
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED' | 'FROZEN'
  openedDate: string
  closedDate?: string
  customerNumber: string
  customerName: string
  profitSharingPercentage?: number
  isDormant: boolean
  lastTransactionDate?: string
}

export interface AccountType {
  id: number
  typeCode: string
  typeName: string
  description: string
  minimumBalance: number
  maintenanceFee: number
  profitSharingRatio?: number
  isSavings: boolean
  isActive: boolean
}

export interface CreateAccountRequest {
  accountTypeId: number
  accountName: string
  initialDeposit: number
  purpose?: string
}

export interface AccountResponse {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  balance: number
  status: string
  openedDate: string
}
```

### 4.3 Transaction Types
```typescript
// src/types/transaction.ts
export interface Transaction {
  id: number
  transactionNumber: string
  accountNumber: string
  accountName: string
  categoryName: string
  amount: number
  transactionType: 'CREDIT' | 'DEBIT'
  description: string
  balanceBefore: number
  balanceAfter: number
  transactionDate: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REVERSED'
  channel: 'TELLER' | 'ATM' | 'MOBILE' | 'WEB'
  toAccountNumber?: string
  toAccountName?: string
}

export interface DepositRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface WithdrawalRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface TransferRequest {
  fromAccountNumber: string
  toAccountNumber: string
  amount: number
  description: string
}

export interface TransactionResponse {
  id: number
  transactionNumber: string
  amount: number
  description: string
  transactionDate: string
  status: string
  balanceAfter: number
}
```

## 5. API Service Layer

### 5.1 API Configuration
```typescript
// src/lib/api.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { getToken, removeTokens } from './auth'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1'

class ApiService {
  private api: AxiosInstance

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 10000,
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    // Request interceptor to add auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = getToken()
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Response interceptor to handle auth errors
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          removeTokens()
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }
    )
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.get(url, config)
    return response.data
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.post(url, data, config)
    return response.data
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.put(url, data, config)
    return response.data
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.delete(url, config)
    return response.data
  }
}

export const apiService = new ApiService()
```

### 5.2 Authentication Service
```typescript
// src/lib/auth.ts
import { apiService } from './api'
import { LoginRequest, RegisterRequest, LoginResponse } from '@/types/auth'
import Cookies from 'js-cookie'

const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEY = 'user'

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiService.post<LoginResponse>('/auth/login', credentials)
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  async register(userData: RegisterRequest): Promise<void> {
    await apiService.post('/auth/register', userData)
  },

  async refreshToken(): Promise<LoginResponse> {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      throw new Error('No refresh token available')
    }
    
    const response = await apiService.post<LoginResponse>('/auth/refresh', { refreshToken })
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  logout() {
    removeTokens()
    removeUser()
  }
}

// Token management functions
export const setTokens = (token: string, refreshToken: string) => {
  Cookies.set(TOKEN_KEY, token, { expires: 1 }) // 1 day
  Cookies.set(REFRESH_TOKEN_KEY, refreshToken, { expires: 7 }) // 7 days
}

export const getToken = (): string | undefined => {
  return Cookies.get(TOKEN_KEY)
}

export const getRefreshToken = (): string | undefined => {
  return Cookies.get(REFRESH_TOKEN_KEY)
}

export const removeTokens = () => {
  Cookies.remove(TOKEN_KEY)
  Cookies.remove(REFRESH_TOKEN_KEY)
}

export const setUser = (user: any) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const getUser = () => {
  if (typeof window === 'undefined') return null
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}

export const removeUser = () => {
  localStorage.removeItem(USER_KEY)
}

export const isAuthenticated = (): boolean => {
  return !!getToken()
}
```

## 6. State Management with Zustand

### 6.1 Auth Store
```typescript
// src/store/authStore.ts
import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'
import { User, LoginRequest, RegisterRequest, AuthState } from '@/types/auth'
import { authService } from '@/lib/auth'

interface AuthStore extends AuthState {
  login: (credentials: LoginRequest) => Promise<void>
  register: (userData: RegisterRequest) => Promise<void>
  logout: () => void
  refreshToken: () => Promise<void>
  setUser: (user: User) => void
  setToken: (token: string, refreshToken: string) => void
  clearAuth: () => void
}

export const useAuthStore = create<AuthStore>()(
  devtools(
    persist(
      (set, get) => ({
        user: null,
        token: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,

        login: async (credentials: LoginRequest) => {
          set({ isLoading: true })
          try {
            const response = await authService.login(credentials)
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
              isLoading: false,
            })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        register: async (userData: RegisterRequest) => {
          set({ isLoading: true })
          try {
            await authService.register(userData)
            set({ isLoading: false })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        logout: () => {
          authService.logout()
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },

        refreshToken: async () => {
          try {
            const response = await authService.refreshToken()
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
            })
          } catch (error) {
            get().logout()
            throw error
          }
        },

        setUser: (user: User) => {
          set({ user })
        },

        setToken: (token: string, refreshToken: string) => {
          set({ token, refreshToken, isAuthenticated: true })
        },

        clearAuth: () => {
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },
      }),
      {
        name: 'auth-storage',
        partialize: (state) => ({
          user: state.user,
          token: state.token,
          refreshToken: state.refreshToken,
          isAuthenticated: state.isAuthenticated,
        }),
      }
    )
  )
)
```

### 6.2 Account Store
```typescript
// src/store/accountStore.ts
import { create } from 'zustand'
import { devtools } from 'zustand/middleware'
import { Account, AccountType, CreateAccountRequest } from '@/types/account'
import { apiService } from '@/lib/api'

interface AccountState {
  accounts: Account[]
  accountTypes: AccountType[]
  selectedAccount: Account | null
  isLoading: boolean
  error: string | null
}

interface AccountActions {
  fetchAccounts: () => Promise<void>
  fetchAccountTypes: () => Promise<void>
  createAccount: (request: CreateAccountRequest) => Promise<void>
  getAccountById: (id: number) => Promise<void>
  getAccountByNumber: (accountNumber: string) => Promise<Account>
  setSelectedAccount: (account: Account | null) => void
  clearError: () => void
}

type AccountStore = AccountState & AccountActions

export const useAccountStore = create<AccountStore>()(
  devtools(
    (set, get) => ({
      accounts: [],
      accountTypes: [],
      selectedAccount: null,
      isLoading: false,
      error: null,

      fetchAccounts: async () => {
        set({ isLoading: true, error: null })
        try {
          const accounts = await apiService.get<Account[]>('/accounts/my-accounts')
          set({ accounts, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch accounts',
            isLoading: false 
          })
        }
      },

      fetchAccountTypes: async () => {
        try {
          const accountTypes = await apiService.get<AccountType[]>('/account-types')
          set({ accountTypes })
        } catch (error: any) {
          set({ error: error.response?.data?.message || 'Failed to fetch account types' })
        }
      },

      createAccount: async (request: CreateAccountRequest) => {
        set({ isLoading: true, error: null })
        try {
          const newAccount = await apiService.post<Account>('/accounts', request)
          set(state => ({ 
            accounts: [...state.accounts, newAccount],
            isLoading: false 
          }))
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to create account',
            isLoading: false 
          })
          throw error
        }
      },

      getAccountById: async (id: number) => {
        set({ isLoading: true, error: null })
        try {
          const account = await apiService.get<Account>(`/accounts/${id}`)
          set({ selectedAccount: account, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch account',
            isLoading: false 
          })
        }
      },

      getAccountByNumber: async (accountNumber: string) => {
        const response = await apiService.get<Account>(`/accounts/number/${accountNumber}`)
        return response
      },

      setSelectedAccount: (account: Account | null) => {
        set({ selectedAccount: account })
      },

      clearError: () => {
        set({ error: null })
      },
    })
  )
)
```

## 7. Custom Hooks

### 7.1 useAuth Hook
```typescript
// src/hooks/useAuth.ts
import { useAuthStore } from '@/store/authStore'
import { useRouter } from 'next/navigation'
import { useEffect } from 'react'

export const useAuth = (requireAuth = true) => {
  const authStore = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (requireAuth && !authStore.isAuthenticated) {
      router.push('/login')
    }
  }, [authStore.isAuthenticated, requireAuth, router])

  return {
    user: authStore.user,
    isAuthenticated: authStore.isAuthenticated,
    isLoading: authStore.isLoading,
    login: authStore.login,
    register: authStore.register,
    logout: authStore.logout,
    refreshToken: authStore.refreshToken,
  }
}
```

### 7.2 useAccounts Hook
```typescript
// src/hooks/useAccounts.ts
import { useAccountStore } from '@/store/accountStore'
import { useEffect } from 'react'

export const useAccounts = () => {
  const accountStore = useAccountStore()

  useEffect(() => {
    accountStore.fetchAccounts()
    accountStore.fetchAccountTypes()
  }, [])

  return {
    accounts: accountStore.accounts,
    accountTypes: accountStore.accountTypes,
    selectedAccount: accountStore.selectedAccount,
    isLoading: accountStore.isLoading,
    error: accountStore.error,
    createAccount: accountStore.createAccount,
    getAccountById: accountStore.getAccountById,
    getAccountByNumber: accountStore.getAccountByNumber,
    setSelectedAccount: accountStore.setSelectedAccount,
    clearError: accountStore.clearError,
  }
}
```

### 7.3 useTransactions Hook
```typescript
// src/hooks/useTransactions.ts
import { useState, useEffect } from 'react'
import { Transaction, DepositRequest, WithdrawalRequest, TransferRequest } from '@/types/transaction'
import { apiService } from '@/lib/api'

export const useTransactions = (accountId?: number) => {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchTransactions = async (accountId?: number) => {
    setIsLoading(true)
    setError(null)
    try {
      const url = accountId ? `/transactions/account/${accountId}` : '/transactions/my-transactions'
      const response = await apiService.get<Transaction[]>(url)
      setTransactions(response)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch transactions')
    } finally {
      setIsLoading(false)
    }
  }

  const deposit = async (request: DepositRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/deposit', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process deposit')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const withdraw = async (request: WithdrawalRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/withdrawal', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process withdrawal')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const transfer = async (request: TransferRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/transfer', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process transfer')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchTransactions(accountId)
  }, [accountId])

  return {
    transactions,
    isLoading,
    error,
    fetchTransactions,
    deposit,
    withdraw,
    transfer,
  }
}
```

## 8. Authentication Components

### 8.1 Login Form
```typescript
// src/components/auth/LoginForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { LoginRequest } from '@/types/auth'
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/24/outline'

const loginSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

export default function LoginForm() {
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const { login } = useAuth(false)
  const router = useRouter()

  const form = useForm<LoginRequest>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  })

  const onSubmit = async (data: LoginRequest) => {
    setIsLoading(true)
    try {
      await login(data)
      router.push('/dashboard')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Login failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Bank Syariah Aisyah
          </CardTitle>
          <CardDescription>
            Masuk ke akun perbankan syariah Anda
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Username</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Masukkan username"
                        {...field}
                        disabled={isLoading}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password</FormLabel>
                    <FormControl>
                      <div className="relative">
                        <Input
                          type={showPassword ? 'text' : 'password'}
                          placeholder="Masukkan password"
                          {...field}
                          disabled={isLoading}
                        />
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                          onClick={() => setShowPassword(!showPassword)}
                          disabled={isLoading}
                        >
                          {showPassword ? (
                            <EyeSlashIcon className="h-4 w-4" />
                          ) : (
                            <EyeIcon className="h-4 w-4" />
                          )}
                        </Button>
                      </div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Memproses...' : 'Masuk'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Belum punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/register')}
            >
              Daftar sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.2 Register Form
```typescript
// src/components/auth/RegisterForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { RegisterRequest } from '@/types/auth'

const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    : '<rootDir>/lib/$1',
    '^@/hooks/(.*)const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    .min(8, 'Password minimal 8 karakter')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/, 
      'Password harus mengandung huruf besar, kecil, angka, dan karakter khusus'),
  fullName: z.string().min(2, 'Nama lengkap minimal 2 karakter'),
  phoneNumber: z.string().regex(/^(\+62|62|0)8[1-9][0-9]{6,9}$/, 'Format nomor HP tidak valid').optional(),
  dateOfBirth: z.string().optional(),
  nationalId: z.string().regex(/^[0-9]{16}$/, 'NIK harus 16 digit angka').optional(),
  address: z.string().max(500, 'Alamat maksimal 500 karakter').optional(),
})

export default function RegisterForm() {
  const [isLoading, setIsLoading] = useState(false)
  const { register } = useAuth(false)
  const router = useRouter()

  const form = useForm<RegisterRequest>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      fullName: '',
      phoneNumber: '',
      dateOfBirth: '',
      nationalId: '',
      address: '',
    },
  })

  const onSubmit = async (data: RegisterRequest) => {
    setIsLoading(true)
    try {
      await register(data)
      router.push('/login?message=Registration successful')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Registration failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100 py-8">
      <Card className="w-full max-w-2xl">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Daftar Akun Baru
          </CardTitle>
          <CardDescription>
            Bergabunglah dengan Bank Syariah Aisyah
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="username"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Username *</FormLabel>
                      <FormControl>
                        <Input placeholder="Username" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="email"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Email *</FormLabel>
                      <FormControl>
                        <Input type="email" placeholder="Email" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password *</FormLabel>
                    <FormControl>
                      <Input type="password" placeholder="Password" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="fullName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Nama Lengkap *</FormLabel>
                    <FormControl>
                      <Input placeholder="Nama lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="phoneNumber"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Nomor HP</FormLabel>
                      <FormControl>
                        <Input placeholder="08xxxxxxxxx" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="dateOfBirth"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Tanggal Lahir</FormLabel>
                      <FormControl>
                        <Input type="date" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="nationalId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>NIK (Nomor Induk Kependudukan)</FormLabel>
                    <FormControl>
                      <Input placeholder="16 digit NIK" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="address"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Alamat</FormLabel>
                    <FormControl>
                      <Input placeholder="Alamat lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Mendaftarkan...' : 'Daftar'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Sudah punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/login')}
            >
              Masuk sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.3 Auth Guard Component
```typescript
// src/components/auth/AuthGuard.tsx
'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'
import { Loading } from '@/components/common/Loading'

interface AuthGuardProps {
  children: React.ReactNode
  requireAuth?: boolean
  requiredRole?: string[]
}

export default function AuthGuard({ 
  children, 
  requireAuth = true, 
  requiredRole 
}: AuthGuardProps) {
  const { isAuthenticated, user, isLoading } = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (!isLoading) {
      if (requireAuth && !isAuthenticated) {
        router.push('/login')
        return
      }

      if (requiredRole && user && !requiredRole.includes(user.role)) {
        router.push('/unauthorized')
        return
      }
    }
  }, [isAuthenticated, user, isLoading, requireAuth, requiredRole, router])

  if (isLoading) {
    return <Loading />
  }

  if (requireAuth && !isAuthenticated) {
    return null
  }

  if (requiredRole && user && !requiredRole.includes(user.role)) {
    return null
  }

  return <>{children}</>
}
```

## 9. Dashboard Components

### 9.1 Dashboard Layout
```typescript
// src/components/dashboard/Sidebar.tsx
'use client'

import { useState } from 'react'
import { usePathname } from 'next/navigation'
import Link from 'next/link'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/hooks/useAuth'
import {
  HomeIcon,
  CreditCardIcon,
  ArrowsRightLeftIcon,
  ChartBarIcon,
  UserIcon,
  Bars3Icon,
  XMarkIcon,
} from '@heroicons/react/24/outline'

const navigation = [
  { name: 'Dashboard', href: '/dashboard', icon: HomeIcon },
  { name: 'Rekening', href: '/dashboard/accounts', icon: CreditCardIcon },
  { name: 'Transaksi', href: '/dashboard/transactions', icon: ArrowsRightLeftIcon },
  { name: 'Laporan', href: '/dashboard/reports', icon: ChartBarIcon },
  { name: 'Profile', href: '/dashboard/profile', icon: UserIcon },
]

export default function Sidebar() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const pathname = usePathname()
  const { user, logout } = useAuth()

  return (
    <>
      {/* Mobile sidebar */}
      <div className={cn(
        'fixed inset-0 flex z-40 md:hidden',
        sidebarOpen ? 'block' : 'hidden'
      )}>
        <div className="fixed inset-0 bg-gray-600 bg-opacity-75" onClick={() => setSidebarOpen(false)} />
        <div className="relative flex-1 flex flex-col max-w-xs w-full bg-white">
          <div className="absolute top-0 right-0 -mr-12 pt-2">
            <Button
              variant="ghost"
              onClick={() => setSidebarOpen(false)}
              className="ml-1 flex items-center justify-center h-10 w-10 rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
            >
              <XMarkIcon className="h-6 w-6 text-white" />
            </Button>
          </div>
          <SidebarContent />
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className="hidden md:flex md:w-64 md:flex-col md:fixed md:inset-y-0">
        <div className="flex-1 flex flex-col min-h-0 border-r border-gray-200 bg-white">
          <SidebarContent />
        </div>
      </div>

      {/* Mobile menu button */}
      <div className="sticky top-0 z-10 md:hidden pl-1 pt-1 sm:pl-3 sm:pt-3 bg-white border-b">
        <Button
          variant="ghost"
          onClick={() => setSidebarOpen(true)}
          className="-ml-0.5 -mt-0.5 h-12 w-12 inline-flex items-center justify-center"
        >
          <Bars3Icon className="h-6 w-6" />
        </Button>
      </div>
    </>
  )

  function SidebarContent() {
    return (
      <>
        <div className="flex-1 flex flex-col pt-5 pb-4 overflow-y-auto">
          <div className="flex items-center flex-shrink-0 px-4">
            <h2 className="text-lg font-semibold text-emerald-800">
              Bank Syariah Aisyah
            </h2>
          </div>
          <nav className="mt-5 flex-1 px-2 space-y-1">
            {navigation.map((item) => {
              const isActive = pathname === item.href
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={cn(
                    'group flex items-center px-2 py-2 text-sm font-medium rounded-md',
                    isActive
                      ? 'bg-emerald-100 text-emerald-900'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  )}
                >
                  <item.icon
                    className={cn(
                      'mr-3 flex-shrink-0 h-6 w-6',
                      isActive ? 'text-emerald-500' : 'text-gray-400 group-hover:text-gray-500'
                    )}
                  />
                  {item.name}
                </Link>
              )
            })}
          </nav>
        </div>
        
        {/* User info and logout */}
        <div className="flex-shrink-0 flex border-t border-gray-200 p-4">
          <div className="flex-shrink-0 w-full group block">
            <div className="flex items-center">
              <div>
                <div className="inline-flex items-center justify-center h-9 w-9 rounded-full bg-emerald-500">
                  <span className="text-sm font-medium leading-none text-white">
                    {user?.fullName?.charAt(0).toUpperCase()}
                  </span>
                </div>
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-700">{user?.fullName}</p>
                <Button
                  variant="link"
                  onClick={logout}
                  className="text-xs text-gray-500 p-0 h-auto"
                >
                  Keluar
                </Button>
              </div>
            </div>
          </div>
        </div>
      </>
    )
  }
}
```

### 9.2 Dashboard Header
```typescript
// src/components/dashboard/Header.tsx
'use client'

import { Bell, Search } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

interface HeaderProps {
  title: string
  description?: string
}

export default function Header({ title, description }: HeaderProps) {
  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center py-6">
          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold leading-7 text-gray-900 sm:truncate">
              {title}
            </h1>
            {description && (
              <p className="mt-1 text-sm text-gray-500">
                {description}
              </p>
            )}
          </div>
          
          <div className="flex items-center space-x-4">
            <div className="flex-1 max-w-lg">
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Search className="h-5 w-5 text-gray-400" />
                </div>
                <Input
                  type="search"
                  placeholder="Cari..."
                  className="pl-10 pr-4 py-2 w-full"
                />
              </div>
            </div>
            
            <Button variant="ghost" size="icon">
              <Bell className="h-5 w-5" />
            </Button>
          </div>
        </div>
      </div>
    </header>
  )
}
```

### 9.3 Statistics Cards
```typescript
// src/components/dashboard/StatCards.tsx
'use client'

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { formatCurrency } from '@/utils/formatters'
import { 
  CreditCardIcon, 
  ArrowUpIcon, 
  ArrowDownIcon,
  BanknotesIcon 
} from '@heroicons/react/24/outline'

interface StatCard {
  title: string
  value: string
  change?: string
  changeType?: 'increase' | 'decrease'
  icon: React.ComponentType<any>
}

interface StatCardsProps {
  totalBalance: number
  totalAccounts: number
  monthlyIncome: number
  monthlyExpense: number
}

export default function StatCards({ 
  totalBalance, 
  totalAccounts, 
  monthlyIncome, 
  monthlyExpense 
}: StatCardsProps) {
  const stats: StatCard[] = [
    {
      title: 'Total Saldo',
      value: formatCurrency(totalBalance),
      icon: BanknotesIcon,
    },
    {
      title: 'Jumlah Rekening',
      value: totalAccounts.toString(),
      icon: CreditCardIcon,
    },
    {
      title: 'Pemasukan Bulan Ini',
      value: formatCurrency(monthlyIncome),
      change: '+12%',
      changeType: 'increase',
      icon: ArrowUpIcon,
    },
    {
      title: 'Pengeluaran Bulan Ini',
      value: formatCurrency(monthlyExpense),
      change: '-5%',
      changeType: 'decrease',
      icon: ArrowDownIcon,
    },
  ]

  return (
    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
      {stats.map((stat) => (
        <Card key={stat.title}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {stat.title}
            </CardTitle>
            <stat.icon className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stat.value}</div>
            {stat.change && (
              <p className={`text-xs ${
                stat.changeType === 'increase' ? 'text-green-600' : 'text-red-600'
              }`}>
                {stat.change} dari bulan lalu
              </p>
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
```

### 9.4 Recent Transactions Component
```typescript
// src/components/dashboard/RecentTransactions.tsx
'use client'

import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Transaction } from '@/types/transaction'
import { apiService } from '@/lib/api'
import { formatCurrency, formatDate } from '@/utils/formatters'
import { ArrowUpIcon, ArrowDownIcon } from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function RecentTransactions() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchRecentTransactions = async () => {
      try {
        const response = await apiService.get<Transaction[]>('/transactions/recent?limit=5')
        setTransactions(response)
      } catch (error) {
        console.error('Failed to fetch recent transactions:', error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchRecentTransactions()
  }, [])

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Transaksi Terbaru</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="flex items-center space-x-4">
                  <div className="w-10 h-10 bg-gray-200 rounded-full"></div>
                  <div className="flex-1 space-y-2">
                    <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                    <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                  </div>
                  <div className="h-4 bg-gray-200 rounded w-20"></div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <div>
          <CardTitle>Transaksi Terbaru</CardTitle>
          <CardDescription>
            {transactions.length} transaksi terakhir
          </CardDescription>
        </div>
        <Button asChild variant="outline" size="sm">
          <Link href="/dashboard/transactions">
            Lihat Semua
          </Link>
        </Button>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {transactions.map((transaction) => (
            <div key={transaction.id} className="flex items-center space-x-4">
              <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
                transaction.transactionType === 'CREDIT' 
                  ? 'bg-green-100' 
                  : 'bg-red-100'
              }`}>
                {transaction.transactionType === 'CREDIT' ? (
                  <ArrowUpIcon className="w-5 h-5 text-green-600" />
                ) : (
                  <ArrowDownIcon className="w-5 h-5 text-red-600" />
                )}
              </div>
              
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 truncate">
                  {transaction.description}
                </p>
                <p className="text-sm text-gray-500">
                  {formatDate(transaction.transactionDate)} • {transaction.accountNumber}
                </p>
              </div>
              
              <div className="text-right">
                <p className={`text-sm font-medium ${
                  transaction.transactionType === 'CREDIT' 
                    ? 'text-green-600' 
                    : 'text-red-600'
                }`}>
                  {transaction.transactionType === 'CREDIT' ? '+' : '-'}
                  {formatCurrency(transaction.amount)}
                </p>
                <p className="text-xs text-gray-500">
                  {transaction.status}
                </p>
              </div>
            </div>
          ))}
          
          {transactions.length === 0 && (
            <div className="text-center py-6">
              <p className="text-gray-500">Belum ada transaksi</p>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
```

## 10. Account Management Components

### 10.1 Account List
```typescript
// src/components/accounts/AccountList.tsx
'use client'

import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Account } from '@/types/account'
import { formatCurrency } from '@/utils/formatters'
import { useAccounts } from '@/hooks/useAccounts'
import { 
  CreditCardIcon, 
  EyeIcon, 
  PlusIcon 
} from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function AccountList() {
  const { accounts, isLoading } = useAccounts()
  const [visibleBalances, setVisibleBalances] = useState<Record<number, boolean>>({})

  const toggleBalanceVisibility = (accountId: number) => {
    setVisibleBalances(prev => ({
      ...prev,
      [accountId]: !prev[accountId]
    }))
  }

  const getStatusBadge = (status: string) => {
    const statusConfig = {
      ACTIVE: { label: 'Aktif', variant: 'default' as const },
      SUSPENDED: { label: 'Ditangguhkan', variant: 'secondary' as const },
      CLOSED: { label: 'Ditutup', variant: 'destructive' as const },
      FROZEN: { label: 'Dibekukan', variant: 'outline' as const },
    }
    
    return statusConfig[status as keyof typeof statusConfig] || {
      label: status,
      variant: 'outline' as const
    }
  }

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Array.from({ length: 3 }).map((_, i) => (
          <Card key={i} className="animate-pulse">
            <CardHeader>
              <div className="h-4 bg-gray-200 rounded w-3/4"></div>
              <div className="h-3 bg-gray-200 rounded w-1/2"></div>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="h-6 bg-gray-200 rounded w-full"></div>
                <div className="h-4 bg-gray-200 rounded w-2/3"></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold">Rekening Saya</h2>
        <Button asChild>
          <Link href="/dashboard/accounts/create">
            <PlusIcon className="w-4 h-4 mr-2" />
            Buka Rekening Baru
          </Link>
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {accounts.map((account) => {
          const statusBadge = getStatusBadge(account.status)
          const isBalanceVisible = visibleBalances[account.id]
          
          return (
            <Card key={account.id} className="hover:shadow-md transition-shadow">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="p-2 bg-emerald-100 rounded-lg">
                      <CreditCardIcon className="w-5 h-5 text-emerald-600" />
                    </div>
                    <div>
                      <CardTitle className="text-sm font-medium">
                        {account.accountName}
                      </CardTitle>
                      <p className="text-xs text-gray-500">
                        {account.accountNumber}
                      </p>
                    </div>
                  </div>
                  <Badge variant={statusBadge.variant}>
                    {statusBadge.label}
                  </Badge>
                </div>
              </CardHeader>
              
              <CardContent className="pt-0">
                <div className="space-y-3">
                  <div>
                    <p className="text-xs text-gray-500 mb-1">Saldo</p>
                    <div className="flex items-center justify-between">
                      <p className="text-lg font-bold">
                        {isBalanceVisible 
                          ? formatCurrency(account.balance)
                          : '••••••••'
                        }
                      </p>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => toggleBalanceVisibility(account.id)}
                      >
                        <EyeIcon className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  
                  <div className="flex justify-between text-xs text-gray-500">
                    <span>Jenis: {account.accountTypeName}</span>
                    <span>{account.currency}</span>
                  </div>
                  
                  <div className="flex space-x-2">
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/accounts/${account.id}`}>
                        Detail
                      </Link>
                    </Button>
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/transactions?account=${account.accountNumber}`}>
                        Transaksi
                      </Link>
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
        
        {accounts.length === 0 && (
          <Card className="col-span-full">
            <CardContent className="text-center py-12">
              <CreditCardIcon className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                Belum ada rekening
              </h3>
              <p className="text-gray-500 mb-6">
                Buka rekening pertama Anda untuk mulai bertransaksi
              </p>
              <Button asChild>
                <Link href="/dashboard/accounts/create">
                  Buka Rekening Sekarang
                </Link>
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
```

## 11. Transaction Components

### 11.1 Transfer Form
```typescript
// src/components/transactions/TransferForm.tsx
'use client'

import { useState } from 'react'
import { useForm# Frontend Guide - Bank Syariah Aisyah Application (Next.js)

## 1. Project Setup

### 1.1 Initialize Next.js Project
```bash
npx create-next-app@latest bank-syariah-aisyah-frontend --typescript --tailwind --eslint --app
cd bank-syariah-aisyah-frontend
```

### 1.2 Package.json Configuration
```json
{
  "name": "bank-syariah-aisyah-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "type-check": "tsc --noEmit",
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage"
  },
  "dependencies": {
    "@headlessui/react": "^1.7.17",
    "@heroicons/react": "^2.0.18",
    "@hookform/resolvers": "^3.3.2",
    "@radix-ui/react-avatar": "^1.0.4",
    "@radix-ui/react-dialog": "^1.0.5",
    "@radix-ui/react-dropdown-menu": "^2.0.6",
    "@radix-ui/react-label": "^2.0.2",
    "@radix-ui/react-popover": "^1.0.7",
    "@radix-ui/react-select": "^2.0.0",
    "@radix-ui/react-slot": "^1.0.2",
    "@radix-ui/react-tabs": "^1.0.4",
    "@radix-ui/react-toast": "^1.1.5",
    "@tanstack/react-query": "^5.8.4",
    "@types/node": "^20",
    "@types/react": "^18",
    "@types/react-dom": "^18",
    "autoprefixer": "^10",
    "axios": "^1.6.2",
    "class-variance-authority": "^0.7.0",
    "clsx": "^2.0.0",
    "date-fns": "^2.30.0",
    "framer-motion": "^10.16.5",
    "js-cookie": "^3.0.5",
    "lucide-react": "^0.294.0",
    "next": "14.0.3",
    "next-auth": "^4.24.5",
    "next-themes": "^0.2.1",
    "react": "^18",
    "react-dom": "^18",
    "react-hook-form": "^7.48.2",
    "recharts": "^2.8.0",
    "tailwind-merge": "^2.0.0",
    "tailwindcss-animate": "^1.0.7",
    "typescript": "^5",
    "zod": "^3.22.4",
    "zustand": "^4.4.7"
  },
  "devDependencies": {
    "@types/js-cookie": "^3.0.6",
    "@types/jest": "^29.5.8",
    "eslint": "^8",
    "eslint-config-next": "14.0.3",
    "jest": "^29.7.0",
    "jest-environment-jsdom": "^29.7.0",
    "postcss": "^8",
    "tailwindcss": "^3.3.6"
  }
}
```

### 1.3 Next.js Configuration
```typescript
// next.config.js
/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    appDir: true,
  },
  images: {
    domains: ['localhost', 'api.aisyahbank.com'],
  },
  env: {
    NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL,
    NEXT_PUBLIC_APP_NAME: 'Bank Syariah Aisyah',
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${process.env.NEXT_PUBLIC_API_BASE_URL}/:path*`,
      },
    ]
  },
}

module.exports = nextConfig
```

### 1.4 Environment Variables
```bash
# .env.local
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_APP_NAME=Bank Syariah Aisyah
NEXTAUTH_SECRET=your-nextauth-secret
NEXTAUTH_URL=http://localhost:3000
```

## 2. Project Structure

```
src/
├── app/
│   ├── (auth)/
│   │   ├── login/
│   │   │   └── page.tsx
│   │   ├── register/
│   │   │   └── page.tsx
│   │   └── layout.tsx
│   ├── (dashboard)/
│   │   ├── accounts/
│   │   │   ├── page.tsx
│   │   │   ├── create/
│   │   │   │   └── page.tsx
│   │   │   └── [id]/
│   │   │       └── page.tsx
│   │   ├── transactions/
│   │   │   ├── page.tsx
│   │   │   ├── transfer/
│   │   │   │   └── page.tsx
│   │   │   ├── deposit/
│   │   │   │   └── page.tsx
│   │   │   └── withdrawal/
│   │   │       └── page.tsx
│   │   ├── reports/
│   │   │   └── page.tsx
│   │   ├── profile/
│   │   │   └── page.tsx
│   │   ├── page.tsx
│   │   └── layout.tsx
│   ├── globals.css
│   ├── layout.tsx
│   ├── loading.tsx
│   ├── error.tsx
│   └── not-found.tsx
├── components/
│   ├── ui/
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── dialog.tsx
│   │   ├── form.tsx
│   │   ├── input.tsx
│   │   ├── label.tsx
│   │   ├── select.tsx
│   │   ├── table.tsx
│   │   ├── tabs.tsx
│   │   └── toast.tsx
│   ├── auth/
│   │   ├── LoginForm.tsx
│   │   ├── RegisterForm.tsx
│   │   └── AuthGuard.tsx
│   ├── dashboard/
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   ├── StatCards.tsx
│   │   └── RecentTransactions.tsx
│   ├── accounts/
│   │   ├── AccountCard.tsx
│   │   ├── AccountList.tsx
│   │   └── CreateAccountForm.tsx
│   ├── transactions/
│   │   ├── TransactionList.tsx
│   │   ├── TransactionForm.tsx
│   │   ├── TransferForm.tsx
│   │   ├── DepositForm.tsx
│   │   └── WithdrawalForm.tsx
│   └── common/
│       ├── Loading.tsx
│       ├── ErrorBoundary.tsx
│       └── Layout.tsx
├── lib/
│   ├── api.ts
│   ├── auth.ts
│   ├── utils.ts
│   ├── validations.ts
│   └── constants.ts
├── hooks/
│   ├── useAuth.ts
│   ├── useAccounts.ts
│   ├── useTransactions.ts
│   ├── useLocalStorage.ts
│   └── useDebounce.ts
├── store/
│   ├── authStore.ts
│   ├── accountStore.ts
│   └── transactionStore.ts
├── types/
│   ├── auth.ts
│   ├── account.ts
│   ├── transaction.ts
│   └── api.ts
└── utils/
    ├── formatters.ts
    ├── validators.ts
    └── helpers.ts
```

## 3. UI Components with shadcn/ui

### 3.1 Install shadcn/ui
```bash
npx shadcn-ui@latest init
npx shadcn-ui@latest add button card dialog form input label select table tabs toast
```

### 3.2 Button Component
```typescript
// src/components/ui/button.tsx
import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "@/lib/utils"

const buttonVariants = cva(
  "inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
        outline: "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
        secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline",
        islamic: "bg-emerald-600 text-white hover:bg-emerald-700",
      },
      size: {
        default: "h-10 px-4 py-2",
        sm: "h-9 rounded-md px-3",
        lg: "h-11 rounded-md px-8",
        icon: "h-10 w-10",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button"
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    )
  }
)
Button.displayName = "Button"

export { Button, buttonVariants }
```

### 3.3 Card Component
```typescript
// src/components/ui/card.tsx
import * as React from "react"
import { cn } from "@/lib/utils"

const Card = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn(
      "rounded-lg border bg-card text-card-foreground shadow-sm",
      className
    )}
    {...props}
  />
))
Card.displayName = "Card"

const CardHeader = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex flex-col space-y-1.5 p-6", className)}
    {...props}
  />
))
CardHeader.displayName = "CardHeader"

const CardTitle = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLHeadingElement>
>(({ className, ...props }, ref) => (
  <h3
    ref={ref}
    className={cn(
      "text-2xl font-semibold leading-none tracking-tight",
      className
    )}
    {...props}
  />
))
CardTitle.displayName = "CardTitle"

const CardDescription = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => (
  <p
    ref={ref}
    className={cn("text-sm text-muted-foreground", className)}
    {...props}
  />
))
CardDescription.displayName = "CardDescription"

const CardContent = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div ref={ref} className={cn("p-6 pt-0", className)} {...props} />
))
CardContent.displayName = "CardContent"

const CardFooter = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex items-center p-6 pt-0", className)}
    {...props}
  />
))
CardFooter.displayName = "CardFooter"

export { Card, CardHeader, CardFooter, CardTitle, CardDescription, CardContent }
```

## 4. Type Definitions

### 4.1 Authentication Types
```typescript
// src/types/auth.ts
export interface User {
  id: number
  username: string
  email: string
  fullName: string
  role: 'ADMIN' | 'MANAGER' | 'TELLER' | 'CUSTOMER'
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
  emailVerified: boolean
  phoneVerified: boolean
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  fullName: string
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface AuthState {
  user: User | null
  token: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoading: boolean
}
```

### 4.2 Account Types
```typescript
// src/types/account.ts
export interface Account {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  accountTypeCode: string
  balance: number
  availableBalance: number
  currency: string
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED' | 'FROZEN'
  openedDate: string
  closedDate?: string
  customerNumber: string
  customerName: string
  profitSharingPercentage?: number
  isDormant: boolean
  lastTransactionDate?: string
}

export interface AccountType {
  id: number
  typeCode: string
  typeName: string
  description: string
  minimumBalance: number
  maintenanceFee: number
  profitSharingRatio?: number
  isSavings: boolean
  isActive: boolean
}

export interface CreateAccountRequest {
  accountTypeId: number
  accountName: string
  initialDeposit: number
  purpose?: string
}

export interface AccountResponse {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  balance: number
  status: string
  openedDate: string
}
```

### 4.3 Transaction Types
```typescript
// src/types/transaction.ts
export interface Transaction {
  id: number
  transactionNumber: string
  accountNumber: string
  accountName: string
  categoryName: string
  amount: number
  transactionType: 'CREDIT' | 'DEBIT'
  description: string
  balanceBefore: number
  balanceAfter: number
  transactionDate: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REVERSED'
  channel: 'TELLER' | 'ATM' | 'MOBILE' | 'WEB'
  toAccountNumber?: string
  toAccountName?: string
}

export interface DepositRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface WithdrawalRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface TransferRequest {
  fromAccountNumber: string
  toAccountNumber: string
  amount: number
  description: string
}

export interface TransactionResponse {
  id: number
  transactionNumber: string
  amount: number
  description: string
  transactionDate: string
  status: string
  balanceAfter: number
}
```

## 5. API Service Layer

### 5.1 API Configuration
```typescript
// src/lib/api.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { getToken, removeTokens } from './auth'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1'

class ApiService {
  private api: AxiosInstance

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 10000,
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    // Request interceptor to add auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = getToken()
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Response interceptor to handle auth errors
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          removeTokens()
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }
    )
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.get(url, config)
    return response.data
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.post(url, data, config)
    return response.data
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.put(url, data, config)
    return response.data
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.delete(url, config)
    return response.data
  }
}

export const apiService = new ApiService()
```

### 5.2 Authentication Service
```typescript
// src/lib/auth.ts
import { apiService } from './api'
import { LoginRequest, RegisterRequest, LoginResponse } from '@/types/auth'
import Cookies from 'js-cookie'

const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEY = 'user'

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiService.post<LoginResponse>('/auth/login', credentials)
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  async register(userData: RegisterRequest): Promise<void> {
    await apiService.post('/auth/register', userData)
  },

  async refreshToken(): Promise<LoginResponse> {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      throw new Error('No refresh token available')
    }
    
    const response = await apiService.post<LoginResponse>('/auth/refresh', { refreshToken })
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  logout() {
    removeTokens()
    removeUser()
  }
}

// Token management functions
export const setTokens = (token: string, refreshToken: string) => {
  Cookies.set(TOKEN_KEY, token, { expires: 1 }) // 1 day
  Cookies.set(REFRESH_TOKEN_KEY, refreshToken, { expires: 7 }) // 7 days
}

export const getToken = (): string | undefined => {
  return Cookies.get(TOKEN_KEY)
}

export const getRefreshToken = (): string | undefined => {
  return Cookies.get(REFRESH_TOKEN_KEY)
}

export const removeTokens = () => {
  Cookies.remove(TOKEN_KEY)
  Cookies.remove(REFRESH_TOKEN_KEY)
}

export const setUser = (user: any) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const getUser = () => {
  if (typeof window === 'undefined') return null
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}

export const removeUser = () => {
  localStorage.removeItem(USER_KEY)
}

export const isAuthenticated = (): boolean => {
  return !!getToken()
}
```

## 6. State Management with Zustand

### 6.1 Auth Store
```typescript
// src/store/authStore.ts
import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'
import { User, LoginRequest, RegisterRequest, AuthState } from '@/types/auth'
import { authService } from '@/lib/auth'

interface AuthStore extends AuthState {
  login: (credentials: LoginRequest) => Promise<void>
  register: (userData: RegisterRequest) => Promise<void>
  logout: () => void
  refreshToken: () => Promise<void>
  setUser: (user: User) => void
  setToken: (token: string, refreshToken: string) => void
  clearAuth: () => void
}

export const useAuthStore = create<AuthStore>()(
  devtools(
    persist(
      (set, get) => ({
        user: null,
        token: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,

        login: async (credentials: LoginRequest) => {
          set({ isLoading: true })
          try {
            const response = await authService.login(credentials)
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
              isLoading: false,
            })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        register: async (userData: RegisterRequest) => {
          set({ isLoading: true })
          try {
            await authService.register(userData)
            set({ isLoading: false })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        logout: () => {
          authService.logout()
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },

        refreshToken: async () => {
          try {
            const response = await authService.refreshToken()
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
            })
          } catch (error) {
            get().logout()
            throw error
          }
        },

        setUser: (user: User) => {
          set({ user })
        },

        setToken: (token: string, refreshToken: string) => {
          set({ token, refreshToken, isAuthenticated: true })
        },

        clearAuth: () => {
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },
      }),
      {
        name: 'auth-storage',
        partialize: (state) => ({
          user: state.user,
          token: state.token,
          refreshToken: state.refreshToken,
          isAuthenticated: state.isAuthenticated,
        }),
      }
    )
  )
)
```

### 6.2 Account Store
```typescript
// src/store/accountStore.ts
import { create } from 'zustand'
import { devtools } from 'zustand/middleware'
import { Account, AccountType, CreateAccountRequest } from '@/types/account'
import { apiService } from '@/lib/api'

interface AccountState {
  accounts: Account[]
  accountTypes: AccountType[]
  selectedAccount: Account | null
  isLoading: boolean
  error: string | null
}

interface AccountActions {
  fetchAccounts: () => Promise<void>
  fetchAccountTypes: () => Promise<void>
  createAccount: (request: CreateAccountRequest) => Promise<void>
  getAccountById: (id: number) => Promise<void>
  getAccountByNumber: (accountNumber: string) => Promise<Account>
  setSelectedAccount: (account: Account | null) => void
  clearError: () => void
}

type AccountStore = AccountState & AccountActions

export const useAccountStore = create<AccountStore>()(
  devtools(
    (set, get) => ({
      accounts: [],
      accountTypes: [],
      selectedAccount: null,
      isLoading: false,
      error: null,

      fetchAccounts: async () => {
        set({ isLoading: true, error: null })
        try {
          const accounts = await apiService.get<Account[]>('/accounts/my-accounts')
          set({ accounts, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch accounts',
            isLoading: false 
          })
        }
      },

      fetchAccountTypes: async () => {
        try {
          const accountTypes = await apiService.get<AccountType[]>('/account-types')
          set({ accountTypes })
        } catch (error: any) {
          set({ error: error.response?.data?.message || 'Failed to fetch account types' })
        }
      },

      createAccount: async (request: CreateAccountRequest) => {
        set({ isLoading: true, error: null })
        try {
          const newAccount = await apiService.post<Account>('/accounts', request)
          set(state => ({ 
            accounts: [...state.accounts, newAccount],
            isLoading: false 
          }))
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to create account',
            isLoading: false 
          })
          throw error
        }
      },

      getAccountById: async (id: number) => {
        set({ isLoading: true, error: null })
        try {
          const account = await apiService.get<Account>(`/accounts/${id}`)
          set({ selectedAccount: account, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch account',
            isLoading: false 
          })
        }
      },

      getAccountByNumber: async (accountNumber: string) => {
        const response = await apiService.get<Account>(`/accounts/number/${accountNumber}`)
        return response
      },

      setSelectedAccount: (account: Account | null) => {
        set({ selectedAccount: account })
      },

      clearError: () => {
        set({ error: null })
      },
    })
  )
)
```

## 7. Custom Hooks

### 7.1 useAuth Hook
```typescript
// src/hooks/useAuth.ts
import { useAuthStore } from '@/store/authStore'
import { useRouter } from 'next/navigation'
import { useEffect } from 'react'

export const useAuth = (requireAuth = true) => {
  const authStore = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (requireAuth && !authStore.isAuthenticated) {
      router.push('/login')
    }
  }, [authStore.isAuthenticated, requireAuth, router])

  return {
    user: authStore.user,
    isAuthenticated: authStore.isAuthenticated,
    isLoading: authStore.isLoading,
    login: authStore.login,
    register: authStore.register,
    logout: authStore.logout,
    refreshToken: authStore.refreshToken,
  }
}
```

### 7.2 useAccounts Hook
```typescript
// src/hooks/useAccounts.ts
import { useAccountStore } from '@/store/accountStore'
import { useEffect } from 'react'

export const useAccounts = () => {
  const accountStore = useAccountStore()

  useEffect(() => {
    accountStore.fetchAccounts()
    accountStore.fetchAccountTypes()
  }, [])

  return {
    accounts: accountStore.accounts,
    accountTypes: accountStore.accountTypes,
    selectedAccount: accountStore.selectedAccount,
    isLoading: accountStore.isLoading,
    error: accountStore.error,
    createAccount: accountStore.createAccount,
    getAccountById: accountStore.getAccountById,
    getAccountByNumber: accountStore.getAccountByNumber,
    setSelectedAccount: accountStore.setSelectedAccount,
    clearError: accountStore.clearError,
  }
}
```

### 7.3 useTransactions Hook
```typescript
// src/hooks/useTransactions.ts
import { useState, useEffect } from 'react'
import { Transaction, DepositRequest, WithdrawalRequest, TransferRequest } from '@/types/transaction'
import { apiService } from '@/lib/api'

export const useTransactions = (accountId?: number) => {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchTransactions = async (accountId?: number) => {
    setIsLoading(true)
    setError(null)
    try {
      const url = accountId ? `/transactions/account/${accountId}` : '/transactions/my-transactions'
      const response = await apiService.get<Transaction[]>(url)
      setTransactions(response)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch transactions')
    } finally {
      setIsLoading(false)
    }
  }

  const deposit = async (request: DepositRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/deposit', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process deposit')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const withdraw = async (request: WithdrawalRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/withdrawal', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process withdrawal')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const transfer = async (request: TransferRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/transfer', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process transfer')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchTransactions(accountId)
  }, [accountId])

  return {
    transactions,
    isLoading,
    error,
    fetchTransactions,
    deposit,
    withdraw,
    transfer,
  }
}
```

## 8. Authentication Components

### 8.1 Login Form
```typescript
// src/components/auth/LoginForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { LoginRequest } from '@/types/auth'
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/24/outline'

const loginSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

export default function LoginForm() {
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const { login } = useAuth(false)
  const router = useRouter()

  const form = useForm<LoginRequest>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  })

  const onSubmit = async (data: LoginRequest) => {
    setIsLoading(true)
    try {
      await login(data)
      router.push('/dashboard')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Login failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Bank Syariah Aisyah
          </CardTitle>
          <CardDescription>
            Masuk ke akun perbankan syariah Anda
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Username</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Masukkan username"
                        {...field}
                        disabled={isLoading}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password</FormLabel>
                    <FormControl>
                      <div className="relative">
                        <Input
                          type={showPassword ? 'text' : 'password'}
                          placeholder="Masukkan password"
                          {...field}
                          disabled={isLoading}
                        />
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                          onClick={() => setShowPassword(!showPassword)}
                          disabled={isLoading}
                        >
                          {showPassword ? (
                            <EyeSlashIcon className="h-4 w-4" />
                          ) : (
                            <EyeIcon className="h-4 w-4" />
                          )}
                        </Button>
                      </div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Memproses...' : 'Masuk'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Belum punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/register')}
            >
              Daftar sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.2 Register Form
```typescript
// src/components/auth/RegisterForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { RegisterRequest } from '@/types/auth'

const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    : '<rootDir>/hooks/$1',
    '^@/store/(.*)const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    .min(8, 'Password minimal 8 karakter')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/, 
      'Password harus mengandung huruf besar, kecil, angka, dan karakter khusus'),
  fullName: z.string().min(2, 'Nama lengkap minimal 2 karakter'),
  phoneNumber: z.string().regex(/^(\+62|62|0)8[1-9][0-9]{6,9}$/, 'Format nomor HP tidak valid').optional(),
  dateOfBirth: z.string().optional(),
  nationalId: z.string().regex(/^[0-9]{16}$/, 'NIK harus 16 digit angka').optional(),
  address: z.string().max(500, 'Alamat maksimal 500 karakter').optional(),
})

export default function RegisterForm() {
  const [isLoading, setIsLoading] = useState(false)
  const { register } = useAuth(false)
  const router = useRouter()

  const form = useForm<RegisterRequest>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      fullName: '',
      phoneNumber: '',
      dateOfBirth: '',
      nationalId: '',
      address: '',
    },
  })

  const onSubmit = async (data: RegisterRequest) => {
    setIsLoading(true)
    try {
      await register(data)
      router.push('/login?message=Registration successful')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Registration failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100 py-8">
      <Card className="w-full max-w-2xl">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Daftar Akun Baru
          </CardTitle>
          <CardDescription>
            Bergabunglah dengan Bank Syariah Aisyah
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="username"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Username *</FormLabel>
                      <FormControl>
                        <Input placeholder="Username" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="email"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Email *</FormLabel>
                      <FormControl>
                        <Input type="email" placeholder="Email" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password *</FormLabel>
                    <FormControl>
                      <Input type="password" placeholder="Password" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="fullName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Nama Lengkap *</FormLabel>
                    <FormControl>
                      <Input placeholder="Nama lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="phoneNumber"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Nomor HP</FormLabel>
                      <FormControl>
                        <Input placeholder="08xxxxxxxxx" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="dateOfBirth"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Tanggal Lahir</FormLabel>
                      <FormControl>
                        <Input type="date" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="nationalId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>NIK (Nomor Induk Kependudukan)</FormLabel>
                    <FormControl>
                      <Input placeholder="16 digit NIK" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="address"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Alamat</FormLabel>
                    <FormControl>
                      <Input placeholder="Alamat lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Mendaftarkan...' : 'Daftar'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Sudah punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/login')}
            >
              Masuk sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.3 Auth Guard Component
```typescript
// src/components/auth/AuthGuard.tsx
'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'
import { Loading } from '@/components/common/Loading'

interface AuthGuardProps {
  children: React.ReactNode
  requireAuth?: boolean
  requiredRole?: string[]
}

export default function AuthGuard({ 
  children, 
  requireAuth = true, 
  requiredRole 
}: AuthGuardProps) {
  const { isAuthenticated, user, isLoading } = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (!isLoading) {
      if (requireAuth && !isAuthenticated) {
        router.push('/login')
        return
      }

      if (requiredRole && user && !requiredRole.includes(user.role)) {
        router.push('/unauthorized')
        return
      }
    }
  }, [isAuthenticated, user, isLoading, requireAuth, requiredRole, router])

  if (isLoading) {
    return <Loading />
  }

  if (requireAuth && !isAuthenticated) {
    return null
  }

  if (requiredRole && user && !requiredRole.includes(user.role)) {
    return null
  }

  return <>{children}</>
}
```

## 9. Dashboard Components

### 9.1 Dashboard Layout
```typescript
// src/components/dashboard/Sidebar.tsx
'use client'

import { useState } from 'react'
import { usePathname } from 'next/navigation'
import Link from 'next/link'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/hooks/useAuth'
import {
  HomeIcon,
  CreditCardIcon,
  ArrowsRightLeftIcon,
  ChartBarIcon,
  UserIcon,
  Bars3Icon,
  XMarkIcon,
} from '@heroicons/react/24/outline'

const navigation = [
  { name: 'Dashboard', href: '/dashboard', icon: HomeIcon },
  { name: 'Rekening', href: '/dashboard/accounts', icon: CreditCardIcon },
  { name: 'Transaksi', href: '/dashboard/transactions', icon: ArrowsRightLeftIcon },
  { name: 'Laporan', href: '/dashboard/reports', icon: ChartBarIcon },
  { name: 'Profile', href: '/dashboard/profile', icon: UserIcon },
]

export default function Sidebar() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const pathname = usePathname()
  const { user, logout } = useAuth()

  return (
    <>
      {/* Mobile sidebar */}
      <div className={cn(
        'fixed inset-0 flex z-40 md:hidden',
        sidebarOpen ? 'block' : 'hidden'
      )}>
        <div className="fixed inset-0 bg-gray-600 bg-opacity-75" onClick={() => setSidebarOpen(false)} />
        <div className="relative flex-1 flex flex-col max-w-xs w-full bg-white">
          <div className="absolute top-0 right-0 -mr-12 pt-2">
            <Button
              variant="ghost"
              onClick={() => setSidebarOpen(false)}
              className="ml-1 flex items-center justify-center h-10 w-10 rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
            >
              <XMarkIcon className="h-6 w-6 text-white" />
            </Button>
          </div>
          <SidebarContent />
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className="hidden md:flex md:w-64 md:flex-col md:fixed md:inset-y-0">
        <div className="flex-1 flex flex-col min-h-0 border-r border-gray-200 bg-white">
          <SidebarContent />
        </div>
      </div>

      {/* Mobile menu button */}
      <div className="sticky top-0 z-10 md:hidden pl-1 pt-1 sm:pl-3 sm:pt-3 bg-white border-b">
        <Button
          variant="ghost"
          onClick={() => setSidebarOpen(true)}
          className="-ml-0.5 -mt-0.5 h-12 w-12 inline-flex items-center justify-center"
        >
          <Bars3Icon className="h-6 w-6" />
        </Button>
      </div>
    </>
  )

  function SidebarContent() {
    return (
      <>
        <div className="flex-1 flex flex-col pt-5 pb-4 overflow-y-auto">
          <div className="flex items-center flex-shrink-0 px-4">
            <h2 className="text-lg font-semibold text-emerald-800">
              Bank Syariah Aisyah
            </h2>
          </div>
          <nav className="mt-5 flex-1 px-2 space-y-1">
            {navigation.map((item) => {
              const isActive = pathname === item.href
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={cn(
                    'group flex items-center px-2 py-2 text-sm font-medium rounded-md',
                    isActive
                      ? 'bg-emerald-100 text-emerald-900'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  )}
                >
                  <item.icon
                    className={cn(
                      'mr-3 flex-shrink-0 h-6 w-6',
                      isActive ? 'text-emerald-500' : 'text-gray-400 group-hover:text-gray-500'
                    )}
                  />
                  {item.name}
                </Link>
              )
            })}
          </nav>
        </div>
        
        {/* User info and logout */}
        <div className="flex-shrink-0 flex border-t border-gray-200 p-4">
          <div className="flex-shrink-0 w-full group block">
            <div className="flex items-center">
              <div>
                <div className="inline-flex items-center justify-center h-9 w-9 rounded-full bg-emerald-500">
                  <span className="text-sm font-medium leading-none text-white">
                    {user?.fullName?.charAt(0).toUpperCase()}
                  </span>
                </div>
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-700">{user?.fullName}</p>
                <Button
                  variant="link"
                  onClick={logout}
                  className="text-xs text-gray-500 p-0 h-auto"
                >
                  Keluar
                </Button>
              </div>
            </div>
          </div>
        </div>
      </>
    )
  }
}
```

### 9.2 Dashboard Header
```typescript
// src/components/dashboard/Header.tsx
'use client'

import { Bell, Search } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

interface HeaderProps {
  title: string
  description?: string
}

export default function Header({ title, description }: HeaderProps) {
  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center py-6">
          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold leading-7 text-gray-900 sm:truncate">
              {title}
            </h1>
            {description && (
              <p className="mt-1 text-sm text-gray-500">
                {description}
              </p>
            )}
          </div>
          
          <div className="flex items-center space-x-4">
            <div className="flex-1 max-w-lg">
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Search className="h-5 w-5 text-gray-400" />
                </div>
                <Input
                  type="search"
                  placeholder="Cari..."
                  className="pl-10 pr-4 py-2 w-full"
                />
              </div>
            </div>
            
            <Button variant="ghost" size="icon">
              <Bell className="h-5 w-5" />
            </Button>
          </div>
        </div>
      </div>
    </header>
  )
}
```

### 9.3 Statistics Cards
```typescript
// src/components/dashboard/StatCards.tsx
'use client'

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { formatCurrency } from '@/utils/formatters'
import { 
  CreditCardIcon, 
  ArrowUpIcon, 
  ArrowDownIcon,
  BanknotesIcon 
} from '@heroicons/react/24/outline'

interface StatCard {
  title: string
  value: string
  change?: string
  changeType?: 'increase' | 'decrease'
  icon: React.ComponentType<any>
}

interface StatCardsProps {
  totalBalance: number
  totalAccounts: number
  monthlyIncome: number
  monthlyExpense: number
}

export default function StatCards({ 
  totalBalance, 
  totalAccounts, 
  monthlyIncome, 
  monthlyExpense 
}: StatCardsProps) {
  const stats: StatCard[] = [
    {
      title: 'Total Saldo',
      value: formatCurrency(totalBalance),
      icon: BanknotesIcon,
    },
    {
      title: 'Jumlah Rekening',
      value: totalAccounts.toString(),
      icon: CreditCardIcon,
    },
    {
      title: 'Pemasukan Bulan Ini',
      value: formatCurrency(monthlyIncome),
      change: '+12%',
      changeType: 'increase',
      icon: ArrowUpIcon,
    },
    {
      title: 'Pengeluaran Bulan Ini',
      value: formatCurrency(monthlyExpense),
      change: '-5%',
      changeType: 'decrease',
      icon: ArrowDownIcon,
    },
  ]

  return (
    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
      {stats.map((stat) => (
        <Card key={stat.title}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {stat.title}
            </CardTitle>
            <stat.icon className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stat.value}</div>
            {stat.change && (
              <p className={`text-xs ${
                stat.changeType === 'increase' ? 'text-green-600' : 'text-red-600'
              }`}>
                {stat.change} dari bulan lalu
              </p>
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
```

### 9.4 Recent Transactions Component
```typescript
// src/components/dashboard/RecentTransactions.tsx
'use client'

import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Transaction } from '@/types/transaction'
import { apiService } from '@/lib/api'
import { formatCurrency, formatDate } from '@/utils/formatters'
import { ArrowUpIcon, ArrowDownIcon } from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function RecentTransactions() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchRecentTransactions = async () => {
      try {
        const response = await apiService.get<Transaction[]>('/transactions/recent?limit=5')
        setTransactions(response)
      } catch (error) {
        console.error('Failed to fetch recent transactions:', error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchRecentTransactions()
  }, [])

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Transaksi Terbaru</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="flex items-center space-x-4">
                  <div className="w-10 h-10 bg-gray-200 rounded-full"></div>
                  <div className="flex-1 space-y-2">
                    <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                    <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                  </div>
                  <div className="h-4 bg-gray-200 rounded w-20"></div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <div>
          <CardTitle>Transaksi Terbaru</CardTitle>
          <CardDescription>
            {transactions.length} transaksi terakhir
          </CardDescription>
        </div>
        <Button asChild variant="outline" size="sm">
          <Link href="/dashboard/transactions">
            Lihat Semua
          </Link>
        </Button>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {transactions.map((transaction) => (
            <div key={transaction.id} className="flex items-center space-x-4">
              <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
                transaction.transactionType === 'CREDIT' 
                  ? 'bg-green-100' 
                  : 'bg-red-100'
              }`}>
                {transaction.transactionType === 'CREDIT' ? (
                  <ArrowUpIcon className="w-5 h-5 text-green-600" />
                ) : (
                  <ArrowDownIcon className="w-5 h-5 text-red-600" />
                )}
              </div>
              
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 truncate">
                  {transaction.description}
                </p>
                <p className="text-sm text-gray-500">
                  {formatDate(transaction.transactionDate)} • {transaction.accountNumber}
                </p>
              </div>
              
              <div className="text-right">
                <p className={`text-sm font-medium ${
                  transaction.transactionType === 'CREDIT' 
                    ? 'text-green-600' 
                    : 'text-red-600'
                }`}>
                  {transaction.transactionType === 'CREDIT' ? '+' : '-'}
                  {formatCurrency(transaction.amount)}
                </p>
                <p className="text-xs text-gray-500">
                  {transaction.status}
                </p>
              </div>
            </div>
          ))}
          
          {transactions.length === 0 && (
            <div className="text-center py-6">
              <p className="text-gray-500">Belum ada transaksi</p>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
```

## 10. Account Management Components

### 10.1 Account List
```typescript
// src/components/accounts/AccountList.tsx
'use client'

import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Account } from '@/types/account'
import { formatCurrency } from '@/utils/formatters'
import { useAccounts } from '@/hooks/useAccounts'
import { 
  CreditCardIcon, 
  EyeIcon, 
  PlusIcon 
} from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function AccountList() {
  const { accounts, isLoading } = useAccounts()
  const [visibleBalances, setVisibleBalances] = useState<Record<number, boolean>>({})

  const toggleBalanceVisibility = (accountId: number) => {
    setVisibleBalances(prev => ({
      ...prev,
      [accountId]: !prev[accountId]
    }))
  }

  const getStatusBadge = (status: string) => {
    const statusConfig = {
      ACTIVE: { label: 'Aktif', variant: 'default' as const },
      SUSPENDED: { label: 'Ditangguhkan', variant: 'secondary' as const },
      CLOSED: { label: 'Ditutup', variant: 'destructive' as const },
      FROZEN: { label: 'Dibekukan', variant: 'outline' as const },
    }
    
    return statusConfig[status as keyof typeof statusConfig] || {
      label: status,
      variant: 'outline' as const
    }
  }

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Array.from({ length: 3 }).map((_, i) => (
          <Card key={i} className="animate-pulse">
            <CardHeader>
              <div className="h-4 bg-gray-200 rounded w-3/4"></div>
              <div className="h-3 bg-gray-200 rounded w-1/2"></div>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="h-6 bg-gray-200 rounded w-full"></div>
                <div className="h-4 bg-gray-200 rounded w-2/3"></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold">Rekening Saya</h2>
        <Button asChild>
          <Link href="/dashboard/accounts/create">
            <PlusIcon className="w-4 h-4 mr-2" />
            Buka Rekening Baru
          </Link>
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {accounts.map((account) => {
          const statusBadge = getStatusBadge(account.status)
          const isBalanceVisible = visibleBalances[account.id]
          
          return (
            <Card key={account.id} className="hover:shadow-md transition-shadow">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="p-2 bg-emerald-100 rounded-lg">
                      <CreditCardIcon className="w-5 h-5 text-emerald-600" />
                    </div>
                    <div>
                      <CardTitle className="text-sm font-medium">
                        {account.accountName}
                      </CardTitle>
                      <p className="text-xs text-gray-500">
                        {account.accountNumber}
                      </p>
                    </div>
                  </div>
                  <Badge variant={statusBadge.variant}>
                    {statusBadge.label}
                  </Badge>
                </div>
              </CardHeader>
              
              <CardContent className="pt-0">
                <div className="space-y-3">
                  <div>
                    <p className="text-xs text-gray-500 mb-1">Saldo</p>
                    <div className="flex items-center justify-between">
                      <p className="text-lg font-bold">
                        {isBalanceVisible 
                          ? formatCurrency(account.balance)
                          : '••••••••'
                        }
                      </p>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => toggleBalanceVisibility(account.id)}
                      >
                        <EyeIcon className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  
                  <div className="flex justify-between text-xs text-gray-500">
                    <span>Jenis: {account.accountTypeName}</span>
                    <span>{account.currency}</span>
                  </div>
                  
                  <div className="flex space-x-2">
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/accounts/${account.id}`}>
                        Detail
                      </Link>
                    </Button>
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/transactions?account=${account.accountNumber}`}>
                        Transaksi
                      </Link>
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
        
        {accounts.length === 0 && (
          <Card className="col-span-full">
            <CardContent className="text-center py-12">
              <CreditCardIcon className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                Belum ada rekening
              </h3>
              <p className="text-gray-500 mb-6">
                Buka rekening pertama Anda untuk mulai bertransaksi
              </p>
              <Button asChild>
                <Link href="/dashboard/accounts/create">
                  Buka Rekening Sekarang
                </Link>
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
```

## 11. Transaction Components

### 11.1 Transfer Form
```typescript
// src/components/transactions/TransferForm.tsx
'use client'

import { useState } from 'react'
import { useForm# Frontend Guide - Bank Syariah Aisyah Application (Next.js)

## 1. Project Setup

### 1.1 Initialize Next.js Project
```bash
npx create-next-app@latest bank-syariah-aisyah-frontend --typescript --tailwind --eslint --app
cd bank-syariah-aisyah-frontend
```

### 1.2 Package.json Configuration
```json
{
  "name": "bank-syariah-aisyah-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "type-check": "tsc --noEmit",
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage"
  },
  "dependencies": {
    "@headlessui/react": "^1.7.17",
    "@heroicons/react": "^2.0.18",
    "@hookform/resolvers": "^3.3.2",
    "@radix-ui/react-avatar": "^1.0.4",
    "@radix-ui/react-dialog": "^1.0.5",
    "@radix-ui/react-dropdown-menu": "^2.0.6",
    "@radix-ui/react-label": "^2.0.2",
    "@radix-ui/react-popover": "^1.0.7",
    "@radix-ui/react-select": "^2.0.0",
    "@radix-ui/react-slot": "^1.0.2",
    "@radix-ui/react-tabs": "^1.0.4",
    "@radix-ui/react-toast": "^1.1.5",
    "@tanstack/react-query": "^5.8.4",
    "@types/node": "^20",
    "@types/react": "^18",
    "@types/react-dom": "^18",
    "autoprefixer": "^10",
    "axios": "^1.6.2",
    "class-variance-authority": "^0.7.0",
    "clsx": "^2.0.0",
    "date-fns": "^2.30.0",
    "framer-motion": "^10.16.5",
    "js-cookie": "^3.0.5",
    "lucide-react": "^0.294.0",
    "next": "14.0.3",
    "next-auth": "^4.24.5",
    "next-themes": "^0.2.1",
    "react": "^18",
    "react-dom": "^18",
    "react-hook-form": "^7.48.2",
    "recharts": "^2.8.0",
    "tailwind-merge": "^2.0.0",
    "tailwindcss-animate": "^1.0.7",
    "typescript": "^5",
    "zod": "^3.22.4",
    "zustand": "^4.4.7"
  },
  "devDependencies": {
    "@types/js-cookie": "^3.0.6",
    "@types/jest": "^29.5.8",
    "eslint": "^8",
    "eslint-config-next": "14.0.3",
    "jest": "^29.7.0",
    "jest-environment-jsdom": "^29.7.0",
    "postcss": "^8",
    "tailwindcss": "^3.3.6"
  }
}
```

### 1.3 Next.js Configuration
```typescript
// next.config.js
/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    appDir: true,
  },
  images: {
    domains: ['localhost', 'api.aisyahbank.com'],
  },
  env: {
    NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL,
    NEXT_PUBLIC_APP_NAME: 'Bank Syariah Aisyah',
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${process.env.NEXT_PUBLIC_API_BASE_URL}/:path*`,
      },
    ]
  },
}

module.exports = nextConfig
```

### 1.4 Environment Variables
```bash
# .env.local
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_APP_NAME=Bank Syariah Aisyah
NEXTAUTH_SECRET=your-nextauth-secret
NEXTAUTH_URL=http://localhost:3000
```

## 2. Project Structure

```
src/
├── app/
│   ├── (auth)/
│   │   ├── login/
│   │   │   └── page.tsx
│   │   ├── register/
│   │   │   └── page.tsx
│   │   └── layout.tsx
│   ├── (dashboard)/
│   │   ├── accounts/
│   │   │   ├── page.tsx
│   │   │   ├── create/
│   │   │   │   └── page.tsx
│   │   │   └── [id]/
│   │   │       └── page.tsx
│   │   ├── transactions/
│   │   │   ├── page.tsx
│   │   │   ├── transfer/
│   │   │   │   └── page.tsx
│   │   │   ├── deposit/
│   │   │   │   └── page.tsx
│   │   │   └── withdrawal/
│   │   │       └── page.tsx
│   │   ├── reports/
│   │   │   └── page.tsx
│   │   ├── profile/
│   │   │   └── page.tsx
│   │   ├── page.tsx
│   │   └── layout.tsx
│   ├── globals.css
│   ├── layout.tsx
│   ├── loading.tsx
│   ├── error.tsx
│   └── not-found.tsx
├── components/
│   ├── ui/
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── dialog.tsx
│   │   ├── form.tsx
│   │   ├── input.tsx
│   │   ├── label.tsx
│   │   ├── select.tsx
│   │   ├── table.tsx
│   │   ├── tabs.tsx
│   │   └── toast.tsx
│   ├── auth/
│   │   ├── LoginForm.tsx
│   │   ├── RegisterForm.tsx
│   │   └── AuthGuard.tsx
│   ├── dashboard/
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   ├── StatCards.tsx
│   │   └── RecentTransactions.tsx
│   ├── accounts/
│   │   ├── AccountCard.tsx
│   │   ├── AccountList.tsx
│   │   └── CreateAccountForm.tsx
│   ├── transactions/
│   │   ├── TransactionList.tsx
│   │   ├── TransactionForm.tsx
│   │   ├── TransferForm.tsx
│   │   ├── DepositForm.tsx
│   │   └── WithdrawalForm.tsx
│   └── common/
│       ├── Loading.tsx
│       ├── ErrorBoundary.tsx
│       └── Layout.tsx
├── lib/
│   ├── api.ts
│   ├── auth.ts
│   ├── utils.ts
│   ├── validations.ts
│   └── constants.ts
├── hooks/
│   ├── useAuth.ts
│   ├── useAccounts.ts
│   ├── useTransactions.ts
│   ├── useLocalStorage.ts
│   └── useDebounce.ts
├── store/
│   ├── authStore.ts
│   ├── accountStore.ts
│   └── transactionStore.ts
├── types/
│   ├── auth.ts
│   ├── account.ts
│   ├── transaction.ts
│   └── api.ts
└── utils/
    ├── formatters.ts
    ├── validators.ts
    └── helpers.ts
```

## 3. UI Components with shadcn/ui

### 3.1 Install shadcn/ui
```bash
npx shadcn-ui@latest init
npx shadcn-ui@latest add button card dialog form input label select table tabs toast
```

### 3.2 Button Component
```typescript
// src/components/ui/button.tsx
import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "@/lib/utils"

const buttonVariants = cva(
  "inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
        outline: "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
        secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline",
        islamic: "bg-emerald-600 text-white hover:bg-emerald-700",
      },
      size: {
        default: "h-10 px-4 py-2",
        sm: "h-9 rounded-md px-3",
        lg: "h-11 rounded-md px-8",
        icon: "h-10 w-10",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button"
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    )
  }
)
Button.displayName = "Button"

export { Button, buttonVariants }
```

### 3.3 Card Component
```typescript
// src/components/ui/card.tsx
import * as React from "react"
import { cn } from "@/lib/utils"

const Card = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn(
      "rounded-lg border bg-card text-card-foreground shadow-sm",
      className
    )}
    {...props}
  />
))
Card.displayName = "Card"

const CardHeader = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex flex-col space-y-1.5 p-6", className)}
    {...props}
  />
))
CardHeader.displayName = "CardHeader"

const CardTitle = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLHeadingElement>
>(({ className, ...props }, ref) => (
  <h3
    ref={ref}
    className={cn(
      "text-2xl font-semibold leading-none tracking-tight",
      className
    )}
    {...props}
  />
))
CardTitle.displayName = "CardTitle"

const CardDescription = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => (
  <p
    ref={ref}
    className={cn("text-sm text-muted-foreground", className)}
    {...props}
  />
))
CardDescription.displayName = "CardDescription"

const CardContent = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div ref={ref} className={cn("p-6 pt-0", className)} {...props} />
))
CardContent.displayName = "CardContent"

const CardFooter = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex items-center p-6 pt-0", className)}
    {...props}
  />
))
CardFooter.displayName = "CardFooter"

export { Card, CardHeader, CardFooter, CardTitle, CardDescription, CardContent }
```

## 4. Type Definitions

### 4.1 Authentication Types
```typescript
// src/types/auth.ts
export interface User {
  id: number
  username: string
  email: string
  fullName: string
  role: 'ADMIN' | 'MANAGER' | 'TELLER' | 'CUSTOMER'
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
  emailVerified: boolean
  phoneVerified: boolean
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  fullName: string
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface AuthState {
  user: User | null
  token: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoading: boolean
}
```

### 4.2 Account Types
```typescript
// src/types/account.ts
export interface Account {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  accountTypeCode: string
  balance: number
  availableBalance: number
  currency: string
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED' | 'FROZEN'
  openedDate: string
  closedDate?: string
  customerNumber: string
  customerName: string
  profitSharingPercentage?: number
  isDormant: boolean
  lastTransactionDate?: string
}

export interface AccountType {
  id: number
  typeCode: string
  typeName: string
  description: string
  minimumBalance: number
  maintenanceFee: number
  profitSharingRatio?: number
  isSavings: boolean
  isActive: boolean
}

export interface CreateAccountRequest {
  accountTypeId: number
  accountName: string
  initialDeposit: number
  purpose?: string
}

export interface AccountResponse {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  balance: number
  status: string
  openedDate: string
}
```

### 4.3 Transaction Types
```typescript
// src/types/transaction.ts
export interface Transaction {
  id: number
  transactionNumber: string
  accountNumber: string
  accountName: string
  categoryName: string
  amount: number
  transactionType: 'CREDIT' | 'DEBIT'
  description: string
  balanceBefore: number
  balanceAfter: number
  transactionDate: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REVERSED'
  channel: 'TELLER' | 'ATM' | 'MOBILE' | 'WEB'
  toAccountNumber?: string
  toAccountName?: string
}

export interface DepositRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface WithdrawalRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface TransferRequest {
  fromAccountNumber: string
  toAccountNumber: string
  amount: number
  description: string
}

export interface TransactionResponse {
  id: number
  transactionNumber: string
  amount: number
  description: string
  transactionDate: string
  status: string
  balanceAfter: number
}
```

## 5. API Service Layer

### 5.1 API Configuration
```typescript
// src/lib/api.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { getToken, removeTokens } from './auth'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1'

class ApiService {
  private api: AxiosInstance

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 10000,
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    // Request interceptor to add auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = getToken()
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Response interceptor to handle auth errors
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          removeTokens()
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }
    )
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.get(url, config)
    return response.data
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.post(url, data, config)
    return response.data
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.put(url, data, config)
    return response.data
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.delete(url, config)
    return response.data
  }
}

export const apiService = new ApiService()
```

### 5.2 Authentication Service
```typescript
// src/lib/auth.ts
import { apiService } from './api'
import { LoginRequest, RegisterRequest, LoginResponse } from '@/types/auth'
import Cookies from 'js-cookie'

const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEY = 'user'

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiService.post<LoginResponse>('/auth/login', credentials)
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  async register(userData: RegisterRequest): Promise<void> {
    await apiService.post('/auth/register', userData)
  },

  async refreshToken(): Promise<LoginResponse> {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      throw new Error('No refresh token available')
    }
    
    const response = await apiService.post<LoginResponse>('/auth/refresh', { refreshToken })
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  logout() {
    removeTokens()
    removeUser()
  }
}

// Token management functions
export const setTokens = (token: string, refreshToken: string) => {
  Cookies.set(TOKEN_KEY, token, { expires: 1 }) // 1 day
  Cookies.set(REFRESH_TOKEN_KEY, refreshToken, { expires: 7 }) // 7 days
}

export const getToken = (): string | undefined => {
  return Cookies.get(TOKEN_KEY)
}

export const getRefreshToken = (): string | undefined => {
  return Cookies.get(REFRESH_TOKEN_KEY)
}

export const removeTokens = () => {
  Cookies.remove(TOKEN_KEY)
  Cookies.remove(REFRESH_TOKEN_KEY)
}

export const setUser = (user: any) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const getUser = () => {
  if (typeof window === 'undefined') return null
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}

export const removeUser = () => {
  localStorage.removeItem(USER_KEY)
}

export const isAuthenticated = (): boolean => {
  return !!getToken()
}
```

## 6. State Management with Zustand

### 6.1 Auth Store
```typescript
// src/store/authStore.ts
import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'
import { User, LoginRequest, RegisterRequest, AuthState } from '@/types/auth'
import { authService } from '@/lib/auth'

interface AuthStore extends AuthState {
  login: (credentials: LoginRequest) => Promise<void>
  register: (userData: RegisterRequest) => Promise<void>
  logout: () => void
  refreshToken: () => Promise<void>
  setUser: (user: User) => void
  setToken: (token: string, refreshToken: string) => void
  clearAuth: () => void
}

export const useAuthStore = create<AuthStore>()(
  devtools(
    persist(
      (set, get) => ({
        user: null,
        token: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,

        login: async (credentials: LoginRequest) => {
          set({ isLoading: true })
          try {
            const response = await authService.login(credentials)
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
              isLoading: false,
            })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        register: async (userData: RegisterRequest) => {
          set({ isLoading: true })
          try {
            await authService.register(userData)
            set({ isLoading: false })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        logout: () => {
          authService.logout()
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },

        refreshToken: async () => {
          try {
            const response = await authService.refreshToken()
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
            })
          } catch (error) {
            get().logout()
            throw error
          }
        },

        setUser: (user: User) => {
          set({ user })
        },

        setToken: (token: string, refreshToken: string) => {
          set({ token, refreshToken, isAuthenticated: true })
        },

        clearAuth: () => {
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },
      }),
      {
        name: 'auth-storage',
        partialize: (state) => ({
          user: state.user,
          token: state.token,
          refreshToken: state.refreshToken,
          isAuthenticated: state.isAuthenticated,
        }),
      }
    )
  )
)
```

### 6.2 Account Store
```typescript
// src/store/accountStore.ts
import { create } from 'zustand'
import { devtools } from 'zustand/middleware'
import { Account, AccountType, CreateAccountRequest } from '@/types/account'
import { apiService } from '@/lib/api'

interface AccountState {
  accounts: Account[]
  accountTypes: AccountType[]
  selectedAccount: Account | null
  isLoading: boolean
  error: string | null
}

interface AccountActions {
  fetchAccounts: () => Promise<void>
  fetchAccountTypes: () => Promise<void>
  createAccount: (request: CreateAccountRequest) => Promise<void>
  getAccountById: (id: number) => Promise<void>
  getAccountByNumber: (accountNumber: string) => Promise<Account>
  setSelectedAccount: (account: Account | null) => void
  clearError: () => void
}

type AccountStore = AccountState & AccountActions

export const useAccountStore = create<AccountStore>()(
  devtools(
    (set, get) => ({
      accounts: [],
      accountTypes: [],
      selectedAccount: null,
      isLoading: false,
      error: null,

      fetchAccounts: async () => {
        set({ isLoading: true, error: null })
        try {
          const accounts = await apiService.get<Account[]>('/accounts/my-accounts')
          set({ accounts, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch accounts',
            isLoading: false 
          })
        }
      },

      fetchAccountTypes: async () => {
        try {
          const accountTypes = await apiService.get<AccountType[]>('/account-types')
          set({ accountTypes })
        } catch (error: any) {
          set({ error: error.response?.data?.message || 'Failed to fetch account types' })
        }
      },

      createAccount: async (request: CreateAccountRequest) => {
        set({ isLoading: true, error: null })
        try {
          const newAccount = await apiService.post<Account>('/accounts', request)
          set(state => ({ 
            accounts: [...state.accounts, newAccount],
            isLoading: false 
          }))
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to create account',
            isLoading: false 
          })
          throw error
        }
      },

      getAccountById: async (id: number) => {
        set({ isLoading: true, error: null })
        try {
          const account = await apiService.get<Account>(`/accounts/${id}`)
          set({ selectedAccount: account, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch account',
            isLoading: false 
          })
        }
      },

      getAccountByNumber: async (accountNumber: string) => {
        const response = await apiService.get<Account>(`/accounts/number/${accountNumber}`)
        return response
      },

      setSelectedAccount: (account: Account | null) => {
        set({ selectedAccount: account })
      },

      clearError: () => {
        set({ error: null })
      },
    })
  )
)
```

## 7. Custom Hooks

### 7.1 useAuth Hook
```typescript
// src/hooks/useAuth.ts
import { useAuthStore } from '@/store/authStore'
import { useRouter } from 'next/navigation'
import { useEffect } from 'react'

export const useAuth = (requireAuth = true) => {
  const authStore = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (requireAuth && !authStore.isAuthenticated) {
      router.push('/login')
    }
  }, [authStore.isAuthenticated, requireAuth, router])

  return {
    user: authStore.user,
    isAuthenticated: authStore.isAuthenticated,
    isLoading: authStore.isLoading,
    login: authStore.login,
    register: authStore.register,
    logout: authStore.logout,
    refreshToken: authStore.refreshToken,
  }
}
```

### 7.2 useAccounts Hook
```typescript
// src/hooks/useAccounts.ts
import { useAccountStore } from '@/store/accountStore'
import { useEffect } from 'react'

export const useAccounts = () => {
  const accountStore = useAccountStore()

  useEffect(() => {
    accountStore.fetchAccounts()
    accountStore.fetchAccountTypes()
  }, [])

  return {
    accounts: accountStore.accounts,
    accountTypes: accountStore.accountTypes,
    selectedAccount: accountStore.selectedAccount,
    isLoading: accountStore.isLoading,
    error: accountStore.error,
    createAccount: accountStore.createAccount,
    getAccountById: accountStore.getAccountById,
    getAccountByNumber: accountStore.getAccountByNumber,
    setSelectedAccount: accountStore.setSelectedAccount,
    clearError: accountStore.clearError,
  }
}
```

### 7.3 useTransactions Hook
```typescript
// src/hooks/useTransactions.ts
import { useState, useEffect } from 'react'
import { Transaction, DepositRequest, WithdrawalRequest, TransferRequest } from '@/types/transaction'
import { apiService } from '@/lib/api'

export const useTransactions = (accountId?: number) => {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchTransactions = async (accountId?: number) => {
    setIsLoading(true)
    setError(null)
    try {
      const url = accountId ? `/transactions/account/${accountId}` : '/transactions/my-transactions'
      const response = await apiService.get<Transaction[]>(url)
      setTransactions(response)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch transactions')
    } finally {
      setIsLoading(false)
    }
  }

  const deposit = async (request: DepositRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/deposit', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process deposit')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const withdraw = async (request: WithdrawalRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/withdrawal', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process withdrawal')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const transfer = async (request: TransferRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/transfer', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process transfer')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchTransactions(accountId)
  }, [accountId])

  return {
    transactions,
    isLoading,
    error,
    fetchTransactions,
    deposit,
    withdraw,
    transfer,
  }
}
```

## 8. Authentication Components

### 8.1 Login Form
```typescript
// src/components/auth/LoginForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { LoginRequest } from '@/types/auth'
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/24/outline'

const loginSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

export default function LoginForm() {
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const { login } = useAuth(false)
  const router = useRouter()

  const form = useForm<LoginRequest>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  })

  const onSubmit = async (data: LoginRequest) => {
    setIsLoading(true)
    try {
      await login(data)
      router.push('/dashboard')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Login failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Bank Syariah Aisyah
          </CardTitle>
          <CardDescription>
            Masuk ke akun perbankan syariah Anda
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Username</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Masukkan username"
                        {...field}
                        disabled={isLoading}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password</FormLabel>
                    <FormControl>
                      <div className="relative">
                        <Input
                          type={showPassword ? 'text' : 'password'}
                          placeholder="Masukkan password"
                          {...field}
                          disabled={isLoading}
                        />
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                          onClick={() => setShowPassword(!showPassword)}
                          disabled={isLoading}
                        >
                          {showPassword ? (
                            <EyeSlashIcon className="h-4 w-4" />
                          ) : (
                            <EyeIcon className="h-4 w-4" />
                          )}
                        </Button>
                      </div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Memproses...' : 'Masuk'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Belum punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/register')}
            >
              Daftar sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.2 Register Form
```typescript
// src/components/auth/RegisterForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { RegisterRequest } from '@/types/auth'

const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    : '<rootDir>/store/$1',
    '^@/types/(.*)const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    .min(8, 'Password minimal 8 karakter')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/, 
      'Password harus mengandung huruf besar, kecil, angka, dan karakter khusus'),
  fullName: z.string().min(2, 'Nama lengkap minimal 2 karakter'),
  phoneNumber: z.string().regex(/^(\+62|62|0)8[1-9][0-9]{6,9}$/, 'Format nomor HP tidak valid').optional(),
  dateOfBirth: z.string().optional(),
  nationalId: z.string().regex(/^[0-9]{16}$/, 'NIK harus 16 digit angka').optional(),
  address: z.string().max(500, 'Alamat maksimal 500 karakter').optional(),
})

export default function RegisterForm() {
  const [isLoading, setIsLoading] = useState(false)
  const { register } = useAuth(false)
  const router = useRouter()

  const form = useForm<RegisterRequest>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      fullName: '',
      phoneNumber: '',
      dateOfBirth: '',
      nationalId: '',
      address: '',
    },
  })

  const onSubmit = async (data: RegisterRequest) => {
    setIsLoading(true)
    try {
      await register(data)
      router.push('/login?message=Registration successful')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Registration failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100 py-8">
      <Card className="w-full max-w-2xl">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Daftar Akun Baru
          </CardTitle>
          <CardDescription>
            Bergabunglah dengan Bank Syariah Aisyah
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="username"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Username *</FormLabel>
                      <FormControl>
                        <Input placeholder="Username" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="email"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Email *</FormLabel>
                      <FormControl>
                        <Input type="email" placeholder="Email" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password *</FormLabel>
                    <FormControl>
                      <Input type="password" placeholder="Password" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="fullName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Nama Lengkap *</FormLabel>
                    <FormControl>
                      <Input placeholder="Nama lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="phoneNumber"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Nomor HP</FormLabel>
                      <FormControl>
                        <Input placeholder="08xxxxxxxxx" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="dateOfBirth"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Tanggal Lahir</FormLabel>
                      <FormControl>
                        <Input type="date" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="nationalId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>NIK (Nomor Induk Kependudukan)</FormLabel>
                    <FormControl>
                      <Input placeholder="16 digit NIK" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="address"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Alamat</FormLabel>
                    <FormControl>
                      <Input placeholder="Alamat lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Mendaftarkan...' : 'Daftar'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Sudah punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/login')}
            >
              Masuk sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.3 Auth Guard Component
```typescript
// src/components/auth/AuthGuard.tsx
'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'
import { Loading } from '@/components/common/Loading'

interface AuthGuardProps {
  children: React.ReactNode
  requireAuth?: boolean
  requiredRole?: string[]
}

export default function AuthGuard({ 
  children, 
  requireAuth = true, 
  requiredRole 
}: AuthGuardProps) {
  const { isAuthenticated, user, isLoading } = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (!isLoading) {
      if (requireAuth && !isAuthenticated) {
        router.push('/login')
        return
      }

      if (requiredRole && user && !requiredRole.includes(user.role)) {
        router.push('/unauthorized')
        return
      }
    }
  }, [isAuthenticated, user, isLoading, requireAuth, requiredRole, router])

  if (isLoading) {
    return <Loading />
  }

  if (requireAuth && !isAuthenticated) {
    return null
  }

  if (requiredRole && user && !requiredRole.includes(user.role)) {
    return null
  }

  return <>{children}</>
}
```

## 9. Dashboard Components

### 9.1 Dashboard Layout
```typescript
// src/components/dashboard/Sidebar.tsx
'use client'

import { useState } from 'react'
import { usePathname } from 'next/navigation'
import Link from 'next/link'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/hooks/useAuth'
import {
  HomeIcon,
  CreditCardIcon,
  ArrowsRightLeftIcon,
  ChartBarIcon,
  UserIcon,
  Bars3Icon,
  XMarkIcon,
} from '@heroicons/react/24/outline'

const navigation = [
  { name: 'Dashboard', href: '/dashboard', icon: HomeIcon },
  { name: 'Rekening', href: '/dashboard/accounts', icon: CreditCardIcon },
  { name: 'Transaksi', href: '/dashboard/transactions', icon: ArrowsRightLeftIcon },
  { name: 'Laporan', href: '/dashboard/reports', icon: ChartBarIcon },
  { name: 'Profile', href: '/dashboard/profile', icon: UserIcon },
]

export default function Sidebar() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const pathname = usePathname()
  const { user, logout } = useAuth()

  return (
    <>
      {/* Mobile sidebar */}
      <div className={cn(
        'fixed inset-0 flex z-40 md:hidden',
        sidebarOpen ? 'block' : 'hidden'
      )}>
        <div className="fixed inset-0 bg-gray-600 bg-opacity-75" onClick={() => setSidebarOpen(false)} />
        <div className="relative flex-1 flex flex-col max-w-xs w-full bg-white">
          <div className="absolute top-0 right-0 -mr-12 pt-2">
            <Button
              variant="ghost"
              onClick={() => setSidebarOpen(false)}
              className="ml-1 flex items-center justify-center h-10 w-10 rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
            >
              <XMarkIcon className="h-6 w-6 text-white" />
            </Button>
          </div>
          <SidebarContent />
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className="hidden md:flex md:w-64 md:flex-col md:fixed md:inset-y-0">
        <div className="flex-1 flex flex-col min-h-0 border-r border-gray-200 bg-white">
          <SidebarContent />
        </div>
      </div>

      {/* Mobile menu button */}
      <div className="sticky top-0 z-10 md:hidden pl-1 pt-1 sm:pl-3 sm:pt-3 bg-white border-b">
        <Button
          variant="ghost"
          onClick={() => setSidebarOpen(true)}
          className="-ml-0.5 -mt-0.5 h-12 w-12 inline-flex items-center justify-center"
        >
          <Bars3Icon className="h-6 w-6" />
        </Button>
      </div>
    </>
  )

  function SidebarContent() {
    return (
      <>
        <div className="flex-1 flex flex-col pt-5 pb-4 overflow-y-auto">
          <div className="flex items-center flex-shrink-0 px-4">
            <h2 className="text-lg font-semibold text-emerald-800">
              Bank Syariah Aisyah
            </h2>
          </div>
          <nav className="mt-5 flex-1 px-2 space-y-1">
            {navigation.map((item) => {
              const isActive = pathname === item.href
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={cn(
                    'group flex items-center px-2 py-2 text-sm font-medium rounded-md',
                    isActive
                      ? 'bg-emerald-100 text-emerald-900'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  )}
                >
                  <item.icon
                    className={cn(
                      'mr-3 flex-shrink-0 h-6 w-6',
                      isActive ? 'text-emerald-500' : 'text-gray-400 group-hover:text-gray-500'
                    )}
                  />
                  {item.name}
                </Link>
              )
            })}
          </nav>
        </div>
        
        {/* User info and logout */}
        <div className="flex-shrink-0 flex border-t border-gray-200 p-4">
          <div className="flex-shrink-0 w-full group block">
            <div className="flex items-center">
              <div>
                <div className="inline-flex items-center justify-center h-9 w-9 rounded-full bg-emerald-500">
                  <span className="text-sm font-medium leading-none text-white">
                    {user?.fullName?.charAt(0).toUpperCase()}
                  </span>
                </div>
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-700">{user?.fullName}</p>
                <Button
                  variant="link"
                  onClick={logout}
                  className="text-xs text-gray-500 p-0 h-auto"
                >
                  Keluar
                </Button>
              </div>
            </div>
          </div>
        </div>
      </>
    )
  }
}
```

### 9.2 Dashboard Header
```typescript
// src/components/dashboard/Header.tsx
'use client'

import { Bell, Search } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

interface HeaderProps {
  title: string
  description?: string
}

export default function Header({ title, description }: HeaderProps) {
  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center py-6">
          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold leading-7 text-gray-900 sm:truncate">
              {title}
            </h1>
            {description && (
              <p className="mt-1 text-sm text-gray-500">
                {description}
              </p>
            )}
          </div>
          
          <div className="flex items-center space-x-4">
            <div className="flex-1 max-w-lg">
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Search className="h-5 w-5 text-gray-400" />
                </div>
                <Input
                  type="search"
                  placeholder="Cari..."
                  className="pl-10 pr-4 py-2 w-full"
                />
              </div>
            </div>
            
            <Button variant="ghost" size="icon">
              <Bell className="h-5 w-5" />
            </Button>
          </div>
        </div>
      </div>
    </header>
  )
}
```

### 9.3 Statistics Cards
```typescript
// src/components/dashboard/StatCards.tsx
'use client'

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { formatCurrency } from '@/utils/formatters'
import { 
  CreditCardIcon, 
  ArrowUpIcon, 
  ArrowDownIcon,
  BanknotesIcon 
} from '@heroicons/react/24/outline'

interface StatCard {
  title: string
  value: string
  change?: string
  changeType?: 'increase' | 'decrease'
  icon: React.ComponentType<any>
}

interface StatCardsProps {
  totalBalance: number
  totalAccounts: number
  monthlyIncome: number
  monthlyExpense: number
}

export default function StatCards({ 
  totalBalance, 
  totalAccounts, 
  monthlyIncome, 
  monthlyExpense 
}: StatCardsProps) {
  const stats: StatCard[] = [
    {
      title: 'Total Saldo',
      value: formatCurrency(totalBalance),
      icon: BanknotesIcon,
    },
    {
      title: 'Jumlah Rekening',
      value: totalAccounts.toString(),
      icon: CreditCardIcon,
    },
    {
      title: 'Pemasukan Bulan Ini',
      value: formatCurrency(monthlyIncome),
      change: '+12%',
      changeType: 'increase',
      icon: ArrowUpIcon,
    },
    {
      title: 'Pengeluaran Bulan Ini',
      value: formatCurrency(monthlyExpense),
      change: '-5%',
      changeType: 'decrease',
      icon: ArrowDownIcon,
    },
  ]

  return (
    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
      {stats.map((stat) => (
        <Card key={stat.title}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {stat.title}
            </CardTitle>
            <stat.icon className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stat.value}</div>
            {stat.change && (
              <p className={`text-xs ${
                stat.changeType === 'increase' ? 'text-green-600' : 'text-red-600'
              }`}>
                {stat.change} dari bulan lalu
              </p>
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
```

### 9.4 Recent Transactions Component
```typescript
// src/components/dashboard/RecentTransactions.tsx
'use client'

import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Transaction } from '@/types/transaction'
import { apiService } from '@/lib/api'
import { formatCurrency, formatDate } from '@/utils/formatters'
import { ArrowUpIcon, ArrowDownIcon } from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function RecentTransactions() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchRecentTransactions = async () => {
      try {
        const response = await apiService.get<Transaction[]>('/transactions/recent?limit=5')
        setTransactions(response)
      } catch (error) {
        console.error('Failed to fetch recent transactions:', error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchRecentTransactions()
  }, [])

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Transaksi Terbaru</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="flex items-center space-x-4">
                  <div className="w-10 h-10 bg-gray-200 rounded-full"></div>
                  <div className="flex-1 space-y-2">
                    <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                    <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                  </div>
                  <div className="h-4 bg-gray-200 rounded w-20"></div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <div>
          <CardTitle>Transaksi Terbaru</CardTitle>
          <CardDescription>
            {transactions.length} transaksi terakhir
          </CardDescription>
        </div>
        <Button asChild variant="outline" size="sm">
          <Link href="/dashboard/transactions">
            Lihat Semua
          </Link>
        </Button>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {transactions.map((transaction) => (
            <div key={transaction.id} className="flex items-center space-x-4">
              <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
                transaction.transactionType === 'CREDIT' 
                  ? 'bg-green-100' 
                  : 'bg-red-100'
              }`}>
                {transaction.transactionType === 'CREDIT' ? (
                  <ArrowUpIcon className="w-5 h-5 text-green-600" />
                ) : (
                  <ArrowDownIcon className="w-5 h-5 text-red-600" />
                )}
              </div>
              
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 truncate">
                  {transaction.description}
                </p>
                <p className="text-sm text-gray-500">
                  {formatDate(transaction.transactionDate)} • {transaction.accountNumber}
                </p>
              </div>
              
              <div className="text-right">
                <p className={`text-sm font-medium ${
                  transaction.transactionType === 'CREDIT' 
                    ? 'text-green-600' 
                    : 'text-red-600'
                }`}>
                  {transaction.transactionType === 'CREDIT' ? '+' : '-'}
                  {formatCurrency(transaction.amount)}
                </p>
                <p className="text-xs text-gray-500">
                  {transaction.status}
                </p>
              </div>
            </div>
          ))}
          
          {transactions.length === 0 && (
            <div className="text-center py-6">
              <p className="text-gray-500">Belum ada transaksi</p>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
```

## 10. Account Management Components

### 10.1 Account List
```typescript
// src/components/accounts/AccountList.tsx
'use client'

import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Account } from '@/types/account'
import { formatCurrency } from '@/utils/formatters'
import { useAccounts } from '@/hooks/useAccounts'
import { 
  CreditCardIcon, 
  EyeIcon, 
  PlusIcon 
} from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function AccountList() {
  const { accounts, isLoading } = useAccounts()
  const [visibleBalances, setVisibleBalances] = useState<Record<number, boolean>>({})

  const toggleBalanceVisibility = (accountId: number) => {
    setVisibleBalances(prev => ({
      ...prev,
      [accountId]: !prev[accountId]
    }))
  }

  const getStatusBadge = (status: string) => {
    const statusConfig = {
      ACTIVE: { label: 'Aktif', variant: 'default' as const },
      SUSPENDED: { label: 'Ditangguhkan', variant: 'secondary' as const },
      CLOSED: { label: 'Ditutup', variant: 'destructive' as const },
      FROZEN: { label: 'Dibekukan', variant: 'outline' as const },
    }
    
    return statusConfig[status as keyof typeof statusConfig] || {
      label: status,
      variant: 'outline' as const
    }
  }

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Array.from({ length: 3 }).map((_, i) => (
          <Card key={i} className="animate-pulse">
            <CardHeader>
              <div className="h-4 bg-gray-200 rounded w-3/4"></div>
              <div className="h-3 bg-gray-200 rounded w-1/2"></div>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="h-6 bg-gray-200 rounded w-full"></div>
                <div className="h-4 bg-gray-200 rounded w-2/3"></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold">Rekening Saya</h2>
        <Button asChild>
          <Link href="/dashboard/accounts/create">
            <PlusIcon className="w-4 h-4 mr-2" />
            Buka Rekening Baru
          </Link>
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {accounts.map((account) => {
          const statusBadge = getStatusBadge(account.status)
          const isBalanceVisible = visibleBalances[account.id]
          
          return (
            <Card key={account.id} className="hover:shadow-md transition-shadow">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="p-2 bg-emerald-100 rounded-lg">
                      <CreditCardIcon className="w-5 h-5 text-emerald-600" />
                    </div>
                    <div>
                      <CardTitle className="text-sm font-medium">
                        {account.accountName}
                      </CardTitle>
                      <p className="text-xs text-gray-500">
                        {account.accountNumber}
                      </p>
                    </div>
                  </div>
                  <Badge variant={statusBadge.variant}>
                    {statusBadge.label}
                  </Badge>
                </div>
              </CardHeader>
              
              <CardContent className="pt-0">
                <div className="space-y-3">
                  <div>
                    <p className="text-xs text-gray-500 mb-1">Saldo</p>
                    <div className="flex items-center justify-between">
                      <p className="text-lg font-bold">
                        {isBalanceVisible 
                          ? formatCurrency(account.balance)
                          : '••••••••'
                        }
                      </p>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => toggleBalanceVisibility(account.id)}
                      >
                        <EyeIcon className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  
                  <div className="flex justify-between text-xs text-gray-500">
                    <span>Jenis: {account.accountTypeName}</span>
                    <span>{account.currency}</span>
                  </div>
                  
                  <div className="flex space-x-2">
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/accounts/${account.id}`}>
                        Detail
                      </Link>
                    </Button>
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/transactions?account=${account.accountNumber}`}>
                        Transaksi
                      </Link>
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
        
        {accounts.length === 0 && (
          <Card className="col-span-full">
            <CardContent className="text-center py-12">
              <CreditCardIcon className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                Belum ada rekening
              </h3>
              <p className="text-gray-500 mb-6">
                Buka rekening pertama Anda untuk mulai bertransaksi
              </p>
              <Button asChild>
                <Link href="/dashboard/accounts/create">
                  Buka Rekening Sekarang
                </Link>
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
```

## 11. Transaction Components

### 11.1 Transfer Form
```typescript
// src/components/transactions/TransferForm.tsx
'use client'

import { useState } from 'react'
import { useForm# Frontend Guide - Bank Syariah Aisyah Application (Next.js)

## 1. Project Setup

### 1.1 Initialize Next.js Project
```bash
npx create-next-app@latest bank-syariah-aisyah-frontend --typescript --tailwind --eslint --app
cd bank-syariah-aisyah-frontend
```

### 1.2 Package.json Configuration
```json
{
  "name": "bank-syariah-aisyah-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "type-check": "tsc --noEmit",
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage"
  },
  "dependencies": {
    "@headlessui/react": "^1.7.17",
    "@heroicons/react": "^2.0.18",
    "@hookform/resolvers": "^3.3.2",
    "@radix-ui/react-avatar": "^1.0.4",
    "@radix-ui/react-dialog": "^1.0.5",
    "@radix-ui/react-dropdown-menu": "^2.0.6",
    "@radix-ui/react-label": "^2.0.2",
    "@radix-ui/react-popover": "^1.0.7",
    "@radix-ui/react-select": "^2.0.0",
    "@radix-ui/react-slot": "^1.0.2",
    "@radix-ui/react-tabs": "^1.0.4",
    "@radix-ui/react-toast": "^1.1.5",
    "@tanstack/react-query": "^5.8.4",
    "@types/node": "^20",
    "@types/react": "^18",
    "@types/react-dom": "^18",
    "autoprefixer": "^10",
    "axios": "^1.6.2",
    "class-variance-authority": "^0.7.0",
    "clsx": "^2.0.0",
    "date-fns": "^2.30.0",
    "framer-motion": "^10.16.5",
    "js-cookie": "^3.0.5",
    "lucide-react": "^0.294.0",
    "next": "14.0.3",
    "next-auth": "^4.24.5",
    "next-themes": "^0.2.1",
    "react": "^18",
    "react-dom": "^18",
    "react-hook-form": "^7.48.2",
    "recharts": "^2.8.0",
    "tailwind-merge": "^2.0.0",
    "tailwindcss-animate": "^1.0.7",
    "typescript": "^5",
    "zod": "^3.22.4",
    "zustand": "^4.4.7"
  },
  "devDependencies": {
    "@types/js-cookie": "^3.0.6",
    "@types/jest": "^29.5.8",
    "eslint": "^8",
    "eslint-config-next": "14.0.3",
    "jest": "^29.7.0",
    "jest-environment-jsdom": "^29.7.0",
    "postcss": "^8",
    "tailwindcss": "^3.3.6"
  }
}
```

### 1.3 Next.js Configuration
```typescript
// next.config.js
/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    appDir: true,
  },
  images: {
    domains: ['localhost', 'api.aisyahbank.com'],
  },
  env: {
    NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL,
    NEXT_PUBLIC_APP_NAME: 'Bank Syariah Aisyah',
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${process.env.NEXT_PUBLIC_API_BASE_URL}/:path*`,
      },
    ]
  },
}

module.exports = nextConfig
```

### 1.4 Environment Variables
```bash
# .env.local
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_APP_NAME=Bank Syariah Aisyah
NEXTAUTH_SECRET=your-nextauth-secret
NEXTAUTH_URL=http://localhost:3000
```

## 2. Project Structure

```
src/
├── app/
│   ├── (auth)/
│   │   ├── login/
│   │   │   └── page.tsx
│   │   ├── register/
│   │   │   └── page.tsx
│   │   └── layout.tsx
│   ├── (dashboard)/
│   │   ├── accounts/
│   │   │   ├── page.tsx
│   │   │   ├── create/
│   │   │   │   └── page.tsx
│   │   │   └── [id]/
│   │   │       └── page.tsx
│   │   ├── transactions/
│   │   │   ├── page.tsx
│   │   │   ├── transfer/
│   │   │   │   └── page.tsx
│   │   │   ├── deposit/
│   │   │   │   └── page.tsx
│   │   │   └── withdrawal/
│   │   │       └── page.tsx
│   │   ├── reports/
│   │   │   └── page.tsx
│   │   ├── profile/
│   │   │   └── page.tsx
│   │   ├── page.tsx
│   │   └── layout.tsx
│   ├── globals.css
│   ├── layout.tsx
│   ├── loading.tsx
│   ├── error.tsx
│   └── not-found.tsx
├── components/
│   ├── ui/
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── dialog.tsx
│   │   ├── form.tsx
│   │   ├── input.tsx
│   │   ├── label.tsx
│   │   ├── select.tsx
│   │   ├── table.tsx
│   │   ├── tabs.tsx
│   │   └── toast.tsx
│   ├── auth/
│   │   ├── LoginForm.tsx
│   │   ├── RegisterForm.tsx
│   │   └── AuthGuard.tsx
│   ├── dashboard/
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   ├── StatCards.tsx
│   │   └── RecentTransactions.tsx
│   ├── accounts/
│   │   ├── AccountCard.tsx
│   │   ├── AccountList.tsx
│   │   └── CreateAccountForm.tsx
│   ├── transactions/
│   │   ├── TransactionList.tsx
│   │   ├── TransactionForm.tsx
│   │   ├── TransferForm.tsx
│   │   ├── DepositForm.tsx
│   │   └── WithdrawalForm.tsx
│   └── common/
│       ├── Loading.tsx
│       ├── ErrorBoundary.tsx
│       └── Layout.tsx
├── lib/
│   ├── api.ts
│   ├── auth.ts
│   ├── utils.ts
│   ├── validations.ts
│   └── constants.ts
├── hooks/
│   ├── useAuth.ts
│   ├── useAccounts.ts
│   ├── useTransactions.ts
│   ├── useLocalStorage.ts
│   └── useDebounce.ts
├── store/
│   ├── authStore.ts
│   ├── accountStore.ts
│   └── transactionStore.ts
├── types/
│   ├── auth.ts
│   ├── account.ts
│   ├── transaction.ts
│   └── api.ts
└── utils/
    ├── formatters.ts
    ├── validators.ts
    └── helpers.ts
```

## 3. UI Components with shadcn/ui

### 3.1 Install shadcn/ui
```bash
npx shadcn-ui@latest init
npx shadcn-ui@latest add button card dialog form input label select table tabs toast
```

### 3.2 Button Component
```typescript
// src/components/ui/button.tsx
import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "@/lib/utils"

const buttonVariants = cva(
  "inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
        outline: "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
        secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline",
        islamic: "bg-emerald-600 text-white hover:bg-emerald-700",
      },
      size: {
        default: "h-10 px-4 py-2",
        sm: "h-9 rounded-md px-3",
        lg: "h-11 rounded-md px-8",
        icon: "h-10 w-10",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button"
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    )
  }
)
Button.displayName = "Button"

export { Button, buttonVariants }
```

### 3.3 Card Component
```typescript
// src/components/ui/card.tsx
import * as React from "react"
import { cn } from "@/lib/utils"

const Card = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn(
      "rounded-lg border bg-card text-card-foreground shadow-sm",
      className
    )}
    {...props}
  />
))
Card.displayName = "Card"

const CardHeader = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex flex-col space-y-1.5 p-6", className)}
    {...props}
  />
))
CardHeader.displayName = "CardHeader"

const CardTitle = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLHeadingElement>
>(({ className, ...props }, ref) => (
  <h3
    ref={ref}
    className={cn(
      "text-2xl font-semibold leading-none tracking-tight",
      className
    )}
    {...props}
  />
))
CardTitle.displayName = "CardTitle"

const CardDescription = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => (
  <p
    ref={ref}
    className={cn("text-sm text-muted-foreground", className)}
    {...props}
  />
))
CardDescription.displayName = "CardDescription"

const CardContent = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div ref={ref} className={cn("p-6 pt-0", className)} {...props} />
))
CardContent.displayName = "CardContent"

const CardFooter = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex items-center p-6 pt-0", className)}
    {...props}
  />
))
CardFooter.displayName = "CardFooter"

export { Card, CardHeader, CardFooter, CardTitle, CardDescription, CardContent }
```

## 4. Type Definitions

### 4.1 Authentication Types
```typescript
// src/types/auth.ts
export interface User {
  id: number
  username: string
  email: string
  fullName: string
  role: 'ADMIN' | 'MANAGER' | 'TELLER' | 'CUSTOMER'
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
  emailVerified: boolean
  phoneVerified: boolean
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  fullName: string
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface AuthState {
  user: User | null
  token: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoading: boolean
}
```

### 4.2 Account Types
```typescript
// src/types/account.ts
export interface Account {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  accountTypeCode: string
  balance: number
  availableBalance: number
  currency: string
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED' | 'FROZEN'
  openedDate: string
  closedDate?: string
  customerNumber: string
  customerName: string
  profitSharingPercentage?: number
  isDormant: boolean
  lastTransactionDate?: string
}

export interface AccountType {
  id: number
  typeCode: string
  typeName: string
  description: string
  minimumBalance: number
  maintenanceFee: number
  profitSharingRatio?: number
  isSavings: boolean
  isActive: boolean
}

export interface CreateAccountRequest {
  accountTypeId: number
  accountName: string
  initialDeposit: number
  purpose?: string
}

export interface AccountResponse {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  balance: number
  status: string
  openedDate: string
}
```

### 4.3 Transaction Types
```typescript
// src/types/transaction.ts
export interface Transaction {
  id: number
  transactionNumber: string
  accountNumber: string
  accountName: string
  categoryName: string
  amount: number
  transactionType: 'CREDIT' | 'DEBIT'
  description: string
  balanceBefore: number
  balanceAfter: number
  transactionDate: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REVERSED'
  channel: 'TELLER' | 'ATM' | 'MOBILE' | 'WEB'
  toAccountNumber?: string
  toAccountName?: string
}

export interface DepositRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface WithdrawalRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface TransferRequest {
  fromAccountNumber: string
  toAccountNumber: string
  amount: number
  description: string
}

export interface TransactionResponse {
  id: number
  transactionNumber: string
  amount: number
  description: string
  transactionDate: string
  status: string
  balanceAfter: number
}
```

## 5. API Service Layer

### 5.1 API Configuration
```typescript
// src/lib/api.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { getToken, removeTokens } from './auth'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1'

class ApiService {
  private api: AxiosInstance

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 10000,
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    // Request interceptor to add auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = getToken()
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Response interceptor to handle auth errors
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          removeTokens()
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }
    )
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.get(url, config)
    return response.data
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.post(url, data, config)
    return response.data
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.put(url, data, config)
    return response.data
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.delete(url, config)
    return response.data
  }
}

export const apiService = new ApiService()
```

### 5.2 Authentication Service
```typescript
// src/lib/auth.ts
import { apiService } from './api'
import { LoginRequest, RegisterRequest, LoginResponse } from '@/types/auth'
import Cookies from 'js-cookie'

const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEY = 'user'

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiService.post<LoginResponse>('/auth/login', credentials)
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  async register(userData: RegisterRequest): Promise<void> {
    await apiService.post('/auth/register', userData)
  },

  async refreshToken(): Promise<LoginResponse> {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      throw new Error('No refresh token available')
    }
    
    const response = await apiService.post<LoginResponse>('/auth/refresh', { refreshToken })
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  logout() {
    removeTokens()
    removeUser()
  }
}

// Token management functions
export const setTokens = (token: string, refreshToken: string) => {
  Cookies.set(TOKEN_KEY, token, { expires: 1 }) // 1 day
  Cookies.set(REFRESH_TOKEN_KEY, refreshToken, { expires: 7 }) // 7 days
}

export const getToken = (): string | undefined => {
  return Cookies.get(TOKEN_KEY)
}

export const getRefreshToken = (): string | undefined => {
  return Cookies.get(REFRESH_TOKEN_KEY)
}

export const removeTokens = () => {
  Cookies.remove(TOKEN_KEY)
  Cookies.remove(REFRESH_TOKEN_KEY)
}

export const setUser = (user: any) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const getUser = () => {
  if (typeof window === 'undefined') return null
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}

export const removeUser = () => {
  localStorage.removeItem(USER_KEY)
}

export const isAuthenticated = (): boolean => {
  return !!getToken()
}
```

## 6. State Management with Zustand

### 6.1 Auth Store
```typescript
// src/store/authStore.ts
import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'
import { User, LoginRequest, RegisterRequest, AuthState } from '@/types/auth'
import { authService } from '@/lib/auth'

interface AuthStore extends AuthState {
  login: (credentials: LoginRequest) => Promise<void>
  register: (userData: RegisterRequest) => Promise<void>
  logout: () => void
  refreshToken: () => Promise<void>
  setUser: (user: User) => void
  setToken: (token: string, refreshToken: string) => void
  clearAuth: () => void
}

export const useAuthStore = create<AuthStore>()(
  devtools(
    persist(
      (set, get) => ({
        user: null,
        token: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,

        login: async (credentials: LoginRequest) => {
          set({ isLoading: true })
          try {
            const response = await authService.login(credentials)
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
              isLoading: false,
            })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        register: async (userData: RegisterRequest) => {
          set({ isLoading: true })
          try {
            await authService.register(userData)
            set({ isLoading: false })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        logout: () => {
          authService.logout()
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },

        refreshToken: async () => {
          try {
            const response = await authService.refreshToken()
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
            })
          } catch (error) {
            get().logout()
            throw error
          }
        },

        setUser: (user: User) => {
          set({ user })
        },

        setToken: (token: string, refreshToken: string) => {
          set({ token, refreshToken, isAuthenticated: true })
        },

        clearAuth: () => {
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },
      }),
      {
        name: 'auth-storage',
        partialize: (state) => ({
          user: state.user,
          token: state.token,
          refreshToken: state.refreshToken,
          isAuthenticated: state.isAuthenticated,
        }),
      }
    )
  )
)
```

### 6.2 Account Store
```typescript
// src/store/accountStore.ts
import { create } from 'zustand'
import { devtools } from 'zustand/middleware'
import { Account, AccountType, CreateAccountRequest } from '@/types/account'
import { apiService } from '@/lib/api'

interface AccountState {
  accounts: Account[]
  accountTypes: AccountType[]
  selectedAccount: Account | null
  isLoading: boolean
  error: string | null
}

interface AccountActions {
  fetchAccounts: () => Promise<void>
  fetchAccountTypes: () => Promise<void>
  createAccount: (request: CreateAccountRequest) => Promise<void>
  getAccountById: (id: number) => Promise<void>
  getAccountByNumber: (accountNumber: string) => Promise<Account>
  setSelectedAccount: (account: Account | null) => void
  clearError: () => void
}

type AccountStore = AccountState & AccountActions

export const useAccountStore = create<AccountStore>()(
  devtools(
    (set, get) => ({
      accounts: [],
      accountTypes: [],
      selectedAccount: null,
      isLoading: false,
      error: null,

      fetchAccounts: async () => {
        set({ isLoading: true, error: null })
        try {
          const accounts = await apiService.get<Account[]>('/accounts/my-accounts')
          set({ accounts, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch accounts',
            isLoading: false 
          })
        }
      },

      fetchAccountTypes: async () => {
        try {
          const accountTypes = await apiService.get<AccountType[]>('/account-types')
          set({ accountTypes })
        } catch (error: any) {
          set({ error: error.response?.data?.message || 'Failed to fetch account types' })
        }
      },

      createAccount: async (request: CreateAccountRequest) => {
        set({ isLoading: true, error: null })
        try {
          const newAccount = await apiService.post<Account>('/accounts', request)
          set(state => ({ 
            accounts: [...state.accounts, newAccount],
            isLoading: false 
          }))
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to create account',
            isLoading: false 
          })
          throw error
        }
      },

      getAccountById: async (id: number) => {
        set({ isLoading: true, error: null })
        try {
          const account = await apiService.get<Account>(`/accounts/${id}`)
          set({ selectedAccount: account, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch account',
            isLoading: false 
          })
        }
      },

      getAccountByNumber: async (accountNumber: string) => {
        const response = await apiService.get<Account>(`/accounts/number/${accountNumber}`)
        return response
      },

      setSelectedAccount: (account: Account | null) => {
        set({ selectedAccount: account })
      },

      clearError: () => {
        set({ error: null })
      },
    })
  )
)
```

## 7. Custom Hooks

### 7.1 useAuth Hook
```typescript
// src/hooks/useAuth.ts
import { useAuthStore } from '@/store/authStore'
import { useRouter } from 'next/navigation'
import { useEffect } from 'react'

export const useAuth = (requireAuth = true) => {
  const authStore = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (requireAuth && !authStore.isAuthenticated) {
      router.push('/login')
    }
  }, [authStore.isAuthenticated, requireAuth, router])

  return {
    user: authStore.user,
    isAuthenticated: authStore.isAuthenticated,
    isLoading: authStore.isLoading,
    login: authStore.login,
    register: authStore.register,
    logout: authStore.logout,
    refreshToken: authStore.refreshToken,
  }
}
```

### 7.2 useAccounts Hook
```typescript
// src/hooks/useAccounts.ts
import { useAccountStore } from '@/store/accountStore'
import { useEffect } from 'react'

export const useAccounts = () => {
  const accountStore = useAccountStore()

  useEffect(() => {
    accountStore.fetchAccounts()
    accountStore.fetchAccountTypes()
  }, [])

  return {
    accounts: accountStore.accounts,
    accountTypes: accountStore.accountTypes,
    selectedAccount: accountStore.selectedAccount,
    isLoading: accountStore.isLoading,
    error: accountStore.error,
    createAccount: accountStore.createAccount,
    getAccountById: accountStore.getAccountById,
    getAccountByNumber: accountStore.getAccountByNumber,
    setSelectedAccount: accountStore.setSelectedAccount,
    clearError: accountStore.clearError,
  }
}
```

### 7.3 useTransactions Hook
```typescript
// src/hooks/useTransactions.ts
import { useState, useEffect } from 'react'
import { Transaction, DepositRequest, WithdrawalRequest, TransferRequest } from '@/types/transaction'
import { apiService } from '@/lib/api'

export const useTransactions = (accountId?: number) => {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchTransactions = async (accountId?: number) => {
    setIsLoading(true)
    setError(null)
    try {
      const url = accountId ? `/transactions/account/${accountId}` : '/transactions/my-transactions'
      const response = await apiService.get<Transaction[]>(url)
      setTransactions(response)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch transactions')
    } finally {
      setIsLoading(false)
    }
  }

  const deposit = async (request: DepositRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/deposit', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process deposit')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const withdraw = async (request: WithdrawalRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/withdrawal', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process withdrawal')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const transfer = async (request: TransferRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/transfer', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process transfer')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchTransactions(accountId)
  }, [accountId])

  return {
    transactions,
    isLoading,
    error,
    fetchTransactions,
    deposit,
    withdraw,
    transfer,
  }
}
```

## 8. Authentication Components

### 8.1 Login Form
```typescript
// src/components/auth/LoginForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { LoginRequest } from '@/types/auth'
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/24/outline'

const loginSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

export default function LoginForm() {
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const { login } = useAuth(false)
  const router = useRouter()

  const form = useForm<LoginRequest>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  })

  const onSubmit = async (data: LoginRequest) => {
    setIsLoading(true)
    try {
      await login(data)
      router.push('/dashboard')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Login failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Bank Syariah Aisyah
          </CardTitle>
          <CardDescription>
            Masuk ke akun perbankan syariah Anda
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Username</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Masukkan username"
                        {...field}
                        disabled={isLoading}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password</FormLabel>
                    <FormControl>
                      <div className="relative">
                        <Input
                          type={showPassword ? 'text' : 'password'}
                          placeholder="Masukkan password"
                          {...field}
                          disabled={isLoading}
                        />
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                          onClick={() => setShowPassword(!showPassword)}
                          disabled={isLoading}
                        >
                          {showPassword ? (
                            <EyeSlashIcon className="h-4 w-4" />
                          ) : (
                            <EyeIcon className="h-4 w-4" />
                          )}
                        </Button>
                      </div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Memproses...' : 'Masuk'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Belum punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/register')}
            >
              Daftar sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.2 Register Form
```typescript
// src/components/auth/RegisterForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { RegisterRequest } from '@/types/auth'

const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    : '<rootDir>/types/$1',
    '^@/utils/(.*)const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    .min(8, 'Password minimal 8 karakter')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/, 
      'Password harus mengandung huruf besar, kecil, angka, dan karakter khusus'),
  fullName: z.string().min(2, 'Nama lengkap minimal 2 karakter'),
  phoneNumber: z.string().regex(/^(\+62|62|0)8[1-9][0-9]{6,9}$/, 'Format nomor HP tidak valid').optional(),
  dateOfBirth: z.string().optional(),
  nationalId: z.string().regex(/^[0-9]{16}$/, 'NIK harus 16 digit angka').optional(),
  address: z.string().max(500, 'Alamat maksimal 500 karakter').optional(),
})

export default function RegisterForm() {
  const [isLoading, setIsLoading] = useState(false)
  const { register } = useAuth(false)
  const router = useRouter()

  const form = useForm<RegisterRequest>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      fullName: '',
      phoneNumber: '',
      dateOfBirth: '',
      nationalId: '',
      address: '',
    },
  })

  const onSubmit = async (data: RegisterRequest) => {
    setIsLoading(true)
    try {
      await register(data)
      router.push('/login?message=Registration successful')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Registration failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100 py-8">
      <Card className="w-full max-w-2xl">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Daftar Akun Baru
          </CardTitle>
          <CardDescription>
            Bergabunglah dengan Bank Syariah Aisyah
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="username"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Username *</FormLabel>
                      <FormControl>
                        <Input placeholder="Username" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="email"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Email *</FormLabel>
                      <FormControl>
                        <Input type="email" placeholder="Email" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password *</FormLabel>
                    <FormControl>
                      <Input type="password" placeholder="Password" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="fullName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Nama Lengkap *</FormLabel>
                    <FormControl>
                      <Input placeholder="Nama lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="phoneNumber"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Nomor HP</FormLabel>
                      <FormControl>
                        <Input placeholder="08xxxxxxxxx" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="dateOfBirth"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Tanggal Lahir</FormLabel>
                      <FormControl>
                        <Input type="date" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="nationalId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>NIK (Nomor Induk Kependudukan)</FormLabel>
                    <FormControl>
                      <Input placeholder="16 digit NIK" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="address"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Alamat</FormLabel>
                    <FormControl>
                      <Input placeholder="Alamat lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Mendaftarkan...' : 'Daftar'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Sudah punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/login')}
            >
              Masuk sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.3 Auth Guard Component
```typescript
// src/components/auth/AuthGuard.tsx
'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'
import { Loading } from '@/components/common/Loading'

interface AuthGuardProps {
  children: React.ReactNode
  requireAuth?: boolean
  requiredRole?: string[]
}

export default function AuthGuard({ 
  children, 
  requireAuth = true, 
  requiredRole 
}: AuthGuardProps) {
  const { isAuthenticated, user, isLoading } = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (!isLoading) {
      if (requireAuth && !isAuthenticated) {
        router.push('/login')
        return
      }

      if (requiredRole && user && !requiredRole.includes(user.role)) {
        router.push('/unauthorized')
        return
      }
    }
  }, [isAuthenticated, user, isLoading, requireAuth, requiredRole, router])

  if (isLoading) {
    return <Loading />
  }

  if (requireAuth && !isAuthenticated) {
    return null
  }

  if (requiredRole && user && !requiredRole.includes(user.role)) {
    return null
  }

  return <>{children}</>
}
```

## 9. Dashboard Components

### 9.1 Dashboard Layout
```typescript
// src/components/dashboard/Sidebar.tsx
'use client'

import { useState } from 'react'
import { usePathname } from 'next/navigation'
import Link from 'next/link'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/hooks/useAuth'
import {
  HomeIcon,
  CreditCardIcon,
  ArrowsRightLeftIcon,
  ChartBarIcon,
  UserIcon,
  Bars3Icon,
  XMarkIcon,
} from '@heroicons/react/24/outline'

const navigation = [
  { name: 'Dashboard', href: '/dashboard', icon: HomeIcon },
  { name: 'Rekening', href: '/dashboard/accounts', icon: CreditCardIcon },
  { name: 'Transaksi', href: '/dashboard/transactions', icon: ArrowsRightLeftIcon },
  { name: 'Laporan', href: '/dashboard/reports', icon: ChartBarIcon },
  { name: 'Profile', href: '/dashboard/profile', icon: UserIcon },
]

export default function Sidebar() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const pathname = usePathname()
  const { user, logout } = useAuth()

  return (
    <>
      {/* Mobile sidebar */}
      <div className={cn(
        'fixed inset-0 flex z-40 md:hidden',
        sidebarOpen ? 'block' : 'hidden'
      )}>
        <div className="fixed inset-0 bg-gray-600 bg-opacity-75" onClick={() => setSidebarOpen(false)} />
        <div className="relative flex-1 flex flex-col max-w-xs w-full bg-white">
          <div className="absolute top-0 right-0 -mr-12 pt-2">
            <Button
              variant="ghost"
              onClick={() => setSidebarOpen(false)}
              className="ml-1 flex items-center justify-center h-10 w-10 rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
            >
              <XMarkIcon className="h-6 w-6 text-white" />
            </Button>
          </div>
          <SidebarContent />
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className="hidden md:flex md:w-64 md:flex-col md:fixed md:inset-y-0">
        <div className="flex-1 flex flex-col min-h-0 border-r border-gray-200 bg-white">
          <SidebarContent />
        </div>
      </div>

      {/* Mobile menu button */}
      <div className="sticky top-0 z-10 md:hidden pl-1 pt-1 sm:pl-3 sm:pt-3 bg-white border-b">
        <Button
          variant="ghost"
          onClick={() => setSidebarOpen(true)}
          className="-ml-0.5 -mt-0.5 h-12 w-12 inline-flex items-center justify-center"
        >
          <Bars3Icon className="h-6 w-6" />
        </Button>
      </div>
    </>
  )

  function SidebarContent() {
    return (
      <>
        <div className="flex-1 flex flex-col pt-5 pb-4 overflow-y-auto">
          <div className="flex items-center flex-shrink-0 px-4">
            <h2 className="text-lg font-semibold text-emerald-800">
              Bank Syariah Aisyah
            </h2>
          </div>
          <nav className="mt-5 flex-1 px-2 space-y-1">
            {navigation.map((item) => {
              const isActive = pathname === item.href
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={cn(
                    'group flex items-center px-2 py-2 text-sm font-medium rounded-md',
                    isActive
                      ? 'bg-emerald-100 text-emerald-900'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  )}
                >
                  <item.icon
                    className={cn(
                      'mr-3 flex-shrink-0 h-6 w-6',
                      isActive ? 'text-emerald-500' : 'text-gray-400 group-hover:text-gray-500'
                    )}
                  />
                  {item.name}
                </Link>
              )
            })}
          </nav>
        </div>
        
        {/* User info and logout */}
        <div className="flex-shrink-0 flex border-t border-gray-200 p-4">
          <div className="flex-shrink-0 w-full group block">
            <div className="flex items-center">
              <div>
                <div className="inline-flex items-center justify-center h-9 w-9 rounded-full bg-emerald-500">
                  <span className="text-sm font-medium leading-none text-white">
                    {user?.fullName?.charAt(0).toUpperCase()}
                  </span>
                </div>
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-700">{user?.fullName}</p>
                <Button
                  variant="link"
                  onClick={logout}
                  className="text-xs text-gray-500 p-0 h-auto"
                >
                  Keluar
                </Button>
              </div>
            </div>
          </div>
        </div>
      </>
    )
  }
}
```

### 9.2 Dashboard Header
```typescript
// src/components/dashboard/Header.tsx
'use client'

import { Bell, Search } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

interface HeaderProps {
  title: string
  description?: string
}

export default function Header({ title, description }: HeaderProps) {
  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center py-6">
          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold leading-7 text-gray-900 sm:truncate">
              {title}
            </h1>
            {description && (
              <p className="mt-1 text-sm text-gray-500">
                {description}
              </p>
            )}
          </div>
          
          <div className="flex items-center space-x-4">
            <div className="flex-1 max-w-lg">
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Search className="h-5 w-5 text-gray-400" />
                </div>
                <Input
                  type="search"
                  placeholder="Cari..."
                  className="pl-10 pr-4 py-2 w-full"
                />
              </div>
            </div>
            
            <Button variant="ghost" size="icon">
              <Bell className="h-5 w-5" />
            </Button>
          </div>
        </div>
      </div>
    </header>
  )
}
```

### 9.3 Statistics Cards
```typescript
// src/components/dashboard/StatCards.tsx
'use client'

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { formatCurrency } from '@/utils/formatters'
import { 
  CreditCardIcon, 
  ArrowUpIcon, 
  ArrowDownIcon,
  BanknotesIcon 
} from '@heroicons/react/24/outline'

interface StatCard {
  title: string
  value: string
  change?: string
  changeType?: 'increase' | 'decrease'
  icon: React.ComponentType<any>
}

interface StatCardsProps {
  totalBalance: number
  totalAccounts: number
  monthlyIncome: number
  monthlyExpense: number
}

export default function StatCards({ 
  totalBalance, 
  totalAccounts, 
  monthlyIncome, 
  monthlyExpense 
}: StatCardsProps) {
  const stats: StatCard[] = [
    {
      title: 'Total Saldo',
      value: formatCurrency(totalBalance),
      icon: BanknotesIcon,
    },
    {
      title: 'Jumlah Rekening',
      value: totalAccounts.toString(),
      icon: CreditCardIcon,
    },
    {
      title: 'Pemasukan Bulan Ini',
      value: formatCurrency(monthlyIncome),
      change: '+12%',
      changeType: 'increase',
      icon: ArrowUpIcon,
    },
    {
      title: 'Pengeluaran Bulan Ini',
      value: formatCurrency(monthlyExpense),
      change: '-5%',
      changeType: 'decrease',
      icon: ArrowDownIcon,
    },
  ]

  return (
    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
      {stats.map((stat) => (
        <Card key={stat.title}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {stat.title}
            </CardTitle>
            <stat.icon className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stat.value}</div>
            {stat.change && (
              <p className={`text-xs ${
                stat.changeType === 'increase' ? 'text-green-600' : 'text-red-600'
              }`}>
                {stat.change} dari bulan lalu
              </p>
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
```

### 9.4 Recent Transactions Component
```typescript
// src/components/dashboard/RecentTransactions.tsx
'use client'

import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Transaction } from '@/types/transaction'
import { apiService } from '@/lib/api'
import { formatCurrency, formatDate } from '@/utils/formatters'
import { ArrowUpIcon, ArrowDownIcon } from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function RecentTransactions() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchRecentTransactions = async () => {
      try {
        const response = await apiService.get<Transaction[]>('/transactions/recent?limit=5')
        setTransactions(response)
      } catch (error) {
        console.error('Failed to fetch recent transactions:', error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchRecentTransactions()
  }, [])

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Transaksi Terbaru</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="flex items-center space-x-4">
                  <div className="w-10 h-10 bg-gray-200 rounded-full"></div>
                  <div className="flex-1 space-y-2">
                    <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                    <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                  </div>
                  <div className="h-4 bg-gray-200 rounded w-20"></div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <div>
          <CardTitle>Transaksi Terbaru</CardTitle>
          <CardDescription>
            {transactions.length} transaksi terakhir
          </CardDescription>
        </div>
        <Button asChild variant="outline" size="sm">
          <Link href="/dashboard/transactions">
            Lihat Semua
          </Link>
        </Button>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {transactions.map((transaction) => (
            <div key={transaction.id} className="flex items-center space-x-4">
              <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
                transaction.transactionType === 'CREDIT' 
                  ? 'bg-green-100' 
                  : 'bg-red-100'
              }`}>
                {transaction.transactionType === 'CREDIT' ? (
                  <ArrowUpIcon className="w-5 h-5 text-green-600" />
                ) : (
                  <ArrowDownIcon className="w-5 h-5 text-red-600" />
                )}
              </div>
              
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 truncate">
                  {transaction.description}
                </p>
                <p className="text-sm text-gray-500">
                  {formatDate(transaction.transactionDate)} • {transaction.accountNumber}
                </p>
              </div>
              
              <div className="text-right">
                <p className={`text-sm font-medium ${
                  transaction.transactionType === 'CREDIT' 
                    ? 'text-green-600' 
                    : 'text-red-600'
                }`}>
                  {transaction.transactionType === 'CREDIT' ? '+' : '-'}
                  {formatCurrency(transaction.amount)}
                </p>
                <p className="text-xs text-gray-500">
                  {transaction.status}
                </p>
              </div>
            </div>
          ))}
          
          {transactions.length === 0 && (
            <div className="text-center py-6">
              <p className="text-gray-500">Belum ada transaksi</p>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
```

## 10. Account Management Components

### 10.1 Account List
```typescript
// src/components/accounts/AccountList.tsx
'use client'

import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Account } from '@/types/account'
import { formatCurrency } from '@/utils/formatters'
import { useAccounts } from '@/hooks/useAccounts'
import { 
  CreditCardIcon, 
  EyeIcon, 
  PlusIcon 
} from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function AccountList() {
  const { accounts, isLoading } = useAccounts()
  const [visibleBalances, setVisibleBalances] = useState<Record<number, boolean>>({})

  const toggleBalanceVisibility = (accountId: number) => {
    setVisibleBalances(prev => ({
      ...prev,
      [accountId]: !prev[accountId]
    }))
  }

  const getStatusBadge = (status: string) => {
    const statusConfig = {
      ACTIVE: { label: 'Aktif', variant: 'default' as const },
      SUSPENDED: { label: 'Ditangguhkan', variant: 'secondary' as const },
      CLOSED: { label: 'Ditutup', variant: 'destructive' as const },
      FROZEN: { label: 'Dibekukan', variant: 'outline' as const },
    }
    
    return statusConfig[status as keyof typeof statusConfig] || {
      label: status,
      variant: 'outline' as const
    }
  }

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Array.from({ length: 3 }).map((_, i) => (
          <Card key={i} className="animate-pulse">
            <CardHeader>
              <div className="h-4 bg-gray-200 rounded w-3/4"></div>
              <div className="h-3 bg-gray-200 rounded w-1/2"></div>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="h-6 bg-gray-200 rounded w-full"></div>
                <div className="h-4 bg-gray-200 rounded w-2/3"></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold">Rekening Saya</h2>
        <Button asChild>
          <Link href="/dashboard/accounts/create">
            <PlusIcon className="w-4 h-4 mr-2" />
            Buka Rekening Baru
          </Link>
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {accounts.map((account) => {
          const statusBadge = getStatusBadge(account.status)
          const isBalanceVisible = visibleBalances[account.id]
          
          return (
            <Card key={account.id} className="hover:shadow-md transition-shadow">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="p-2 bg-emerald-100 rounded-lg">
                      <CreditCardIcon className="w-5 h-5 text-emerald-600" />
                    </div>
                    <div>
                      <CardTitle className="text-sm font-medium">
                        {account.accountName}
                      </CardTitle>
                      <p className="text-xs text-gray-500">
                        {account.accountNumber}
                      </p>
                    </div>
                  </div>
                  <Badge variant={statusBadge.variant}>
                    {statusBadge.label}
                  </Badge>
                </div>
              </CardHeader>
              
              <CardContent className="pt-0">
                <div className="space-y-3">
                  <div>
                    <p className="text-xs text-gray-500 mb-1">Saldo</p>
                    <div className="flex items-center justify-between">
                      <p className="text-lg font-bold">
                        {isBalanceVisible 
                          ? formatCurrency(account.balance)
                          : '••••••••'
                        }
                      </p>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => toggleBalanceVisibility(account.id)}
                      >
                        <EyeIcon className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  
                  <div className="flex justify-between text-xs text-gray-500">
                    <span>Jenis: {account.accountTypeName}</span>
                    <span>{account.currency}</span>
                  </div>
                  
                  <div className="flex space-x-2">
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/accounts/${account.id}`}>
                        Detail
                      </Link>
                    </Button>
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/transactions?account=${account.accountNumber}`}>
                        Transaksi
                      </Link>
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
        
        {accounts.length === 0 && (
          <Card className="col-span-full">
            <CardContent className="text-center py-12">
              <CreditCardIcon className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                Belum ada rekening
              </h3>
              <p className="text-gray-500 mb-6">
                Buka rekening pertama Anda untuk mulai bertransaksi
              </p>
              <Button asChild>
                <Link href="/dashboard/accounts/create">
                  Buka Rekening Sekarang
                </Link>
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
```

## 11. Transaction Components

### 11.1 Transfer Form
```typescript
// src/components/transactions/TransferForm.tsx
'use client'

import { useState } from 'react'
import { useForm# Frontend Guide - Bank Syariah Aisyah Application (Next.js)

## 1. Project Setup

### 1.1 Initialize Next.js Project
```bash
npx create-next-app@latest bank-syariah-aisyah-frontend --typescript --tailwind --eslint --app
cd bank-syariah-aisyah-frontend
```

### 1.2 Package.json Configuration
```json
{
  "name": "bank-syariah-aisyah-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "type-check": "tsc --noEmit",
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage"
  },
  "dependencies": {
    "@headlessui/react": "^1.7.17",
    "@heroicons/react": "^2.0.18",
    "@hookform/resolvers": "^3.3.2",
    "@radix-ui/react-avatar": "^1.0.4",
    "@radix-ui/react-dialog": "^1.0.5",
    "@radix-ui/react-dropdown-menu": "^2.0.6",
    "@radix-ui/react-label": "^2.0.2",
    "@radix-ui/react-popover": "^1.0.7",
    "@radix-ui/react-select": "^2.0.0",
    "@radix-ui/react-slot": "^1.0.2",
    "@radix-ui/react-tabs": "^1.0.4",
    "@radix-ui/react-toast": "^1.1.5",
    "@tanstack/react-query": "^5.8.4",
    "@types/node": "^20",
    "@types/react": "^18",
    "@types/react-dom": "^18",
    "autoprefixer": "^10",
    "axios": "^1.6.2",
    "class-variance-authority": "^0.7.0",
    "clsx": "^2.0.0",
    "date-fns": "^2.30.0",
    "framer-motion": "^10.16.5",
    "js-cookie": "^3.0.5",
    "lucide-react": "^0.294.0",
    "next": "14.0.3",
    "next-auth": "^4.24.5",
    "next-themes": "^0.2.1",
    "react": "^18",
    "react-dom": "^18",
    "react-hook-form": "^7.48.2",
    "recharts": "^2.8.0",
    "tailwind-merge": "^2.0.0",
    "tailwindcss-animate": "^1.0.7",
    "typescript": "^5",
    "zod": "^3.22.4",
    "zustand": "^4.4.7"
  },
  "devDependencies": {
    "@types/js-cookie": "^3.0.6",
    "@types/jest": "^29.5.8",
    "eslint": "^8",
    "eslint-config-next": "14.0.3",
    "jest": "^29.7.0",
    "jest-environment-jsdom": "^29.7.0",
    "postcss": "^8",
    "tailwindcss": "^3.3.6"
  }
}
```

### 1.3 Next.js Configuration
```typescript
// next.config.js
/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    appDir: true,
  },
  images: {
    domains: ['localhost', 'api.aisyahbank.com'],
  },
  env: {
    NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL,
    NEXT_PUBLIC_APP_NAME: 'Bank Syariah Aisyah',
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${process.env.NEXT_PUBLIC_API_BASE_URL}/:path*`,
      },
    ]
  },
}

module.exports = nextConfig
```

### 1.4 Environment Variables
```bash
# .env.local
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_APP_NAME=Bank Syariah Aisyah
NEXTAUTH_SECRET=your-nextauth-secret
NEXTAUTH_URL=http://localhost:3000
```

## 2. Project Structure

```
src/
├── app/
│   ├── (auth)/
│   │   ├── login/
│   │   │   └── page.tsx
│   │   ├── register/
│   │   │   └── page.tsx
│   │   └── layout.tsx
│   ├── (dashboard)/
│   │   ├── accounts/
│   │   │   ├── page.tsx
│   │   │   ├── create/
│   │   │   │   └── page.tsx
│   │   │   └── [id]/
│   │   │       └── page.tsx
│   │   ├── transactions/
│   │   │   ├── page.tsx
│   │   │   ├── transfer/
│   │   │   │   └── page.tsx
│   │   │   ├── deposit/
│   │   │   │   └── page.tsx
│   │   │   └── withdrawal/
│   │   │       └── page.tsx
│   │   ├── reports/
│   │   │   └── page.tsx
│   │   ├── profile/
│   │   │   └── page.tsx
│   │   ├── page.tsx
│   │   └── layout.tsx
│   ├── globals.css
│   ├── layout.tsx
│   ├── loading.tsx
│   ├── error.tsx
│   └── not-found.tsx
├── components/
│   ├── ui/
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── dialog.tsx
│   │   ├── form.tsx
│   │   ├── input.tsx
│   │   ├── label.tsx
│   │   ├── select.tsx
│   │   ├── table.tsx
│   │   ├── tabs.tsx
│   │   └── toast.tsx
│   ├── auth/
│   │   ├── LoginForm.tsx
│   │   ├── RegisterForm.tsx
│   │   └── AuthGuard.tsx
│   ├── dashboard/
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   ├── StatCards.tsx
│   │   └── RecentTransactions.tsx
│   ├── accounts/
│   │   ├── AccountCard.tsx
│   │   ├── AccountList.tsx
│   │   └── CreateAccountForm.tsx
│   ├── transactions/
│   │   ├── TransactionList.tsx
│   │   ├── TransactionForm.tsx
│   │   ├── TransferForm.tsx
│   │   ├── DepositForm.tsx
│   │   └── WithdrawalForm.tsx
│   └── common/
│       ├── Loading.tsx
│       ├── ErrorBoundary.tsx
│       └── Layout.tsx
├── lib/
│   ├── api.ts
│   ├── auth.ts
│   ├── utils.ts
│   ├── validations.ts
│   └── constants.ts
├── hooks/
│   ├── useAuth.ts
│   ├── useAccounts.ts
│   ├── useTransactions.ts
│   ├── useLocalStorage.ts
│   └── useDebounce.ts
├── store/
│   ├── authStore.ts
│   ├── accountStore.ts
│   └── transactionStore.ts
├── types/
│   ├── auth.ts
│   ├── account.ts
│   ├── transaction.ts
│   └── api.ts
└── utils/
    ├── formatters.ts
    ├── validators.ts
    └── helpers.ts
```

## 3. UI Components with shadcn/ui

### 3.1 Install shadcn/ui
```bash
npx shadcn-ui@latest init
npx shadcn-ui@latest add button card dialog form input label select table tabs toast
```

### 3.2 Button Component
```typescript
// src/components/ui/button.tsx
import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "@/lib/utils"

const buttonVariants = cva(
  "inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
        outline: "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
        secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline",
        islamic: "bg-emerald-600 text-white hover:bg-emerald-700",
      },
      size: {
        default: "h-10 px-4 py-2",
        sm: "h-9 rounded-md px-3",
        lg: "h-11 rounded-md px-8",
        icon: "h-10 w-10",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button"
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    )
  }
)
Button.displayName = "Button"

export { Button, buttonVariants }
```

### 3.3 Card Component
```typescript
// src/components/ui/card.tsx
import * as React from "react"
import { cn } from "@/lib/utils"

const Card = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn(
      "rounded-lg border bg-card text-card-foreground shadow-sm",
      className
    )}
    {...props}
  />
))
Card.displayName = "Card"

const CardHeader = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex flex-col space-y-1.5 p-6", className)}
    {...props}
  />
))
CardHeader.displayName = "CardHeader"

const CardTitle = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLHeadingElement>
>(({ className, ...props }, ref) => (
  <h3
    ref={ref}
    className={cn(
      "text-2xl font-semibold leading-none tracking-tight",
      className
    )}
    {...props}
  />
))
CardTitle.displayName = "CardTitle"

const CardDescription = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => (
  <p
    ref={ref}
    className={cn("text-sm text-muted-foreground", className)}
    {...props}
  />
))
CardDescription.displayName = "CardDescription"

const CardContent = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div ref={ref} className={cn("p-6 pt-0", className)} {...props} />
))
CardContent.displayName = "CardContent"

const CardFooter = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex items-center p-6 pt-0", className)}
    {...props}
  />
))
CardFooter.displayName = "CardFooter"

export { Card, CardHeader, CardFooter, CardTitle, CardDescription, CardContent }
```

## 4. Type Definitions

### 4.1 Authentication Types
```typescript
// src/types/auth.ts
export interface User {
  id: number
  username: string
  email: string
  fullName: string
  role: 'ADMIN' | 'MANAGER' | 'TELLER' | 'CUSTOMER'
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
  emailVerified: boolean
  phoneVerified: boolean
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  fullName: string
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface AuthState {
  user: User | null
  token: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoading: boolean
}
```

### 4.2 Account Types
```typescript
// src/types/account.ts
export interface Account {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  accountTypeCode: string
  balance: number
  availableBalance: number
  currency: string
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED' | 'FROZEN'
  openedDate: string
  closedDate?: string
  customerNumber: string
  customerName: string
  profitSharingPercentage?: number
  isDormant: boolean
  lastTransactionDate?: string
}

export interface AccountType {
  id: number
  typeCode: string
  typeName: string
  description: string
  minimumBalance: number
  maintenanceFee: number
  profitSharingRatio?: number
  isSavings: boolean
  isActive: boolean
}

export interface CreateAccountRequest {
  accountTypeId: number
  accountName: string
  initialDeposit: number
  purpose?: string
}

export interface AccountResponse {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  balance: number
  status: string
  openedDate: string
}
```

### 4.3 Transaction Types
```typescript
// src/types/transaction.ts
export interface Transaction {
  id: number
  transactionNumber: string
  accountNumber: string
  accountName: string
  categoryName: string
  amount: number
  transactionType: 'CREDIT' | 'DEBIT'
  description: string
  balanceBefore: number
  balanceAfter: number
  transactionDate: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REVERSED'
  channel: 'TELLER' | 'ATM' | 'MOBILE' | 'WEB'
  toAccountNumber?: string
  toAccountName?: string
}

export interface DepositRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface WithdrawalRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface TransferRequest {
  fromAccountNumber: string
  toAccountNumber: string
  amount: number
  description: string
}

export interface TransactionResponse {
  id: number
  transactionNumber: string
  amount: number
  description: string
  transactionDate: string
  status: string
  balanceAfter: number
}
```

## 5. API Service Layer

### 5.1 API Configuration
```typescript
// src/lib/api.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { getToken, removeTokens } from './auth'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1'

class ApiService {
  private api: AxiosInstance

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 10000,
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    // Request interceptor to add auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = getToken()
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Response interceptor to handle auth errors
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          removeTokens()
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }
    )
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.get(url, config)
    return response.data
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.post(url, data, config)
    return response.data
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.put(url, data, config)
    return response.data
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.delete(url, config)
    return response.data
  }
}

export const apiService = new ApiService()
```

### 5.2 Authentication Service
```typescript
// src/lib/auth.ts
import { apiService } from './api'
import { LoginRequest, RegisterRequest, LoginResponse } from '@/types/auth'
import Cookies from 'js-cookie'

const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEY = 'user'

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiService.post<LoginResponse>('/auth/login', credentials)
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  async register(userData: RegisterRequest): Promise<void> {
    await apiService.post('/auth/register', userData)
  },

  async refreshToken(): Promise<LoginResponse> {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      throw new Error('No refresh token available')
    }
    
    const response = await apiService.post<LoginResponse>('/auth/refresh', { refreshToken })
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  logout() {
    removeTokens()
    removeUser()
  }
}

// Token management functions
export const setTokens = (token: string, refreshToken: string) => {
  Cookies.set(TOKEN_KEY, token, { expires: 1 }) // 1 day
  Cookies.set(REFRESH_TOKEN_KEY, refreshToken, { expires: 7 }) // 7 days
}

export const getToken = (): string | undefined => {
  return Cookies.get(TOKEN_KEY)
}

export const getRefreshToken = (): string | undefined => {
  return Cookies.get(REFRESH_TOKEN_KEY)
}

export const removeTokens = () => {
  Cookies.remove(TOKEN_KEY)
  Cookies.remove(REFRESH_TOKEN_KEY)
}

export const setUser = (user: any) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const getUser = () => {
  if (typeof window === 'undefined') return null
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}

export const removeUser = () => {
  localStorage.removeItem(USER_KEY)
}

export const isAuthenticated = (): boolean => {
  return !!getToken()
}
```

## 6. State Management with Zustand

### 6.1 Auth Store
```typescript
// src/store/authStore.ts
import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'
import { User, LoginRequest, RegisterRequest, AuthState } from '@/types/auth'
import { authService } from '@/lib/auth'

interface AuthStore extends AuthState {
  login: (credentials: LoginRequest) => Promise<void>
  register: (userData: RegisterRequest) => Promise<void>
  logout: () => void
  refreshToken: () => Promise<void>
  setUser: (user: User) => void
  setToken: (token: string, refreshToken: string) => void
  clearAuth: () => void
}

export const useAuthStore = create<AuthStore>()(
  devtools(
    persist(
      (set, get) => ({
        user: null,
        token: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,

        login: async (credentials: LoginRequest) => {
          set({ isLoading: true })
          try {
            const response = await authService.login(credentials)
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
              isLoading: false,
            })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        register: async (userData: RegisterRequest) => {
          set({ isLoading: true })
          try {
            await authService.register(userData)
            set({ isLoading: false })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        logout: () => {
          authService.logout()
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },

        refreshToken: async () => {
          try {
            const response = await authService.refreshToken()
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
            })
          } catch (error) {
            get().logout()
            throw error
          }
        },

        setUser: (user: User) => {
          set({ user })
        },

        setToken: (token: string, refreshToken: string) => {
          set({ token, refreshToken, isAuthenticated: true })
        },

        clearAuth: () => {
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },
      }),
      {
        name: 'auth-storage',
        partialize: (state) => ({
          user: state.user,
          token: state.token,
          refreshToken: state.refreshToken,
          isAuthenticated: state.isAuthenticated,
        }),
      }
    )
  )
)
```

### 6.2 Account Store
```typescript
// src/store/accountStore.ts
import { create } from 'zustand'
import { devtools } from 'zustand/middleware'
import { Account, AccountType, CreateAccountRequest } from '@/types/account'
import { apiService } from '@/lib/api'

interface AccountState {
  accounts: Account[]
  accountTypes: AccountType[]
  selectedAccount: Account | null
  isLoading: boolean
  error: string | null
}

interface AccountActions {
  fetchAccounts: () => Promise<void>
  fetchAccountTypes: () => Promise<void>
  createAccount: (request: CreateAccountRequest) => Promise<void>
  getAccountById: (id: number) => Promise<void>
  getAccountByNumber: (accountNumber: string) => Promise<Account>
  setSelectedAccount: (account: Account | null) => void
  clearError: () => void
}

type AccountStore = AccountState & AccountActions

export const useAccountStore = create<AccountStore>()(
  devtools(
    (set, get) => ({
      accounts: [],
      accountTypes: [],
      selectedAccount: null,
      isLoading: false,
      error: null,

      fetchAccounts: async () => {
        set({ isLoading: true, error: null })
        try {
          const accounts = await apiService.get<Account[]>('/accounts/my-accounts')
          set({ accounts, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch accounts',
            isLoading: false 
          })
        }
      },

      fetchAccountTypes: async () => {
        try {
          const accountTypes = await apiService.get<AccountType[]>('/account-types')
          set({ accountTypes })
        } catch (error: any) {
          set({ error: error.response?.data?.message || 'Failed to fetch account types' })
        }
      },

      createAccount: async (request: CreateAccountRequest) => {
        set({ isLoading: true, error: null })
        try {
          const newAccount = await apiService.post<Account>('/accounts', request)
          set(state => ({ 
            accounts: [...state.accounts, newAccount],
            isLoading: false 
          }))
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to create account',
            isLoading: false 
          })
          throw error
        }
      },

      getAccountById: async (id: number) => {
        set({ isLoading: true, error: null })
        try {
          const account = await apiService.get<Account>(`/accounts/${id}`)
          set({ selectedAccount: account, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch account',
            isLoading: false 
          })
        }
      },

      getAccountByNumber: async (accountNumber: string) => {
        const response = await apiService.get<Account>(`/accounts/number/${accountNumber}`)
        return response
      },

      setSelectedAccount: (account: Account | null) => {
        set({ selectedAccount: account })
      },

      clearError: () => {
        set({ error: null })
      },
    })
  )
)
```

## 7. Custom Hooks

### 7.1 useAuth Hook
```typescript
// src/hooks/useAuth.ts
import { useAuthStore } from '@/store/authStore'
import { useRouter } from 'next/navigation'
import { useEffect } from 'react'

export const useAuth = (requireAuth = true) => {
  const authStore = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (requireAuth && !authStore.isAuthenticated) {
      router.push('/login')
    }
  }, [authStore.isAuthenticated, requireAuth, router])

  return {
    user: authStore.user,
    isAuthenticated: authStore.isAuthenticated,
    isLoading: authStore.isLoading,
    login: authStore.login,
    register: authStore.register,
    logout: authStore.logout,
    refreshToken: authStore.refreshToken,
  }
}
```

### 7.2 useAccounts Hook
```typescript
// src/hooks/useAccounts.ts
import { useAccountStore } from '@/store/accountStore'
import { useEffect } from 'react'

export const useAccounts = () => {
  const accountStore = useAccountStore()

  useEffect(() => {
    accountStore.fetchAccounts()
    accountStore.fetchAccountTypes()
  }, [])

  return {
    accounts: accountStore.accounts,
    accountTypes: accountStore.accountTypes,
    selectedAccount: accountStore.selectedAccount,
    isLoading: accountStore.isLoading,
    error: accountStore.error,
    createAccount: accountStore.createAccount,
    getAccountById: accountStore.getAccountById,
    getAccountByNumber: accountStore.getAccountByNumber,
    setSelectedAccount: accountStore.setSelectedAccount,
    clearError: accountStore.clearError,
  }
}
```

### 7.3 useTransactions Hook
```typescript
// src/hooks/useTransactions.ts
import { useState, useEffect } from 'react'
import { Transaction, DepositRequest, WithdrawalRequest, TransferRequest } from '@/types/transaction'
import { apiService } from '@/lib/api'

export const useTransactions = (accountId?: number) => {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchTransactions = async (accountId?: number) => {
    setIsLoading(true)
    setError(null)
    try {
      const url = accountId ? `/transactions/account/${accountId}` : '/transactions/my-transactions'
      const response = await apiService.get<Transaction[]>(url)
      setTransactions(response)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch transactions')
    } finally {
      setIsLoading(false)
    }
  }

  const deposit = async (request: DepositRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/deposit', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process deposit')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const withdraw = async (request: WithdrawalRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/withdrawal', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process withdrawal')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const transfer = async (request: TransferRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/transfer', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process transfer')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchTransactions(accountId)
  }, [accountId])

  return {
    transactions,
    isLoading,
    error,
    fetchTransactions,
    deposit,
    withdraw,
    transfer,
  }
}
```

## 8. Authentication Components

### 8.1 Login Form
```typescript
// src/components/auth/LoginForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { LoginRequest } from '@/types/auth'
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/24/outline'

const loginSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

export default function LoginForm() {
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const { login } = useAuth(false)
  const router = useRouter()

  const form = useForm<LoginRequest>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  })

  const onSubmit = async (data: LoginRequest) => {
    setIsLoading(true)
    try {
      await login(data)
      router.push('/dashboard')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Login failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Bank Syariah Aisyah
          </CardTitle>
          <CardDescription>
            Masuk ke akun perbankan syariah Anda
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Username</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Masukkan username"
                        {...field}
                        disabled={isLoading}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password</FormLabel>
                    <FormControl>
                      <div className="relative">
                        <Input
                          type={showPassword ? 'text' : 'password'}
                          placeholder="Masukkan password"
                          {...field}
                          disabled={isLoading}
                        />
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                          onClick={() => setShowPassword(!showPassword)}
                          disabled={isLoading}
                        >
                          {showPassword ? (
                            <EyeSlashIcon className="h-4 w-4" />
                          ) : (
                            <EyeIcon className="h-4 w-4" />
                          )}
                        </Button>
                      </div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Memproses...' : 'Masuk'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Belum punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/register')}
            >
              Daftar sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.2 Register Form
```typescript
// src/components/auth/RegisterForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { RegisterRequest } from '@/types/auth'

const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    : '<rootDir>/utils/$1',
  },
  testEnvironment: 'jest-environment-jsdom',
}

module.exports = createJestConfig(customJestConfig)
```

### 16.2 Test Setup
```javascript
// jest.setup.js
import '@testing-library/jest-dom'

// Mock next/navigation
jest.mock('next/navigation', () => ({
  useRouter() {
    return {
      push: jest.fn(),
      replace: jest.fn(),
      prefetch: jest.fn(),
      back: jest.fn(),
    }
  },
  usePathname() {
    return ''
  },
  useSearchParams() {
    return new URLSearchParams()
  },
}))

// Mock next/image
jest.mock('next/image', () => ({
  __esModule: true,
  default: (props) => {
    return React.createElement('img', props)
  },
}))
```

### 16.3 Example Component Test
```typescript
// src/components/__tests__/LoginForm.test.tsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import LoginForm from '@/components/auth/LoginForm'
import { useAuth } from '@/hooks/useAuth'

// Mock the useAuth hook
jest.mock('@/hooks/useAuth')
const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>

describe('LoginForm', () => {
  const mockLogin = jest.fn()

  beforeEach(() => {
    mockUseAuth.mockReturnValue({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      login: mockLogin,
      register: jest.fn(),
      logout: jest.fn(),
      refreshToken: jest.fn(),
    })
  })

  afterEach(() => {
    jest.clearAllMocks()
  })

  it('renders login form correctly', () => {
    render(<LoginForm />)
    
    expect(screen.getByText('Bank Syariah Aisyah')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Masukkan username')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Masukkan password')).toBeInTheDocument()
    expect(screen.getByText('Masuk')).toBeInTheDocument()
  })

  it('validates required fields', async () => {
    render(<LoginForm />)
    
    const submitButton = screen.getByText('Masuk')
    fireEvent.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText('Username must be at least 3 characters')).toBeInTheDocument()
      expect(screen.getByText('Password must be at least 8 characters')).toBeInTheDocument()
    })
  })

  it('calls login function with correct data', async () => {
    render(<LoginForm />)
    
    const usernameInput = screen.getByPlaceholderText('Masukkan username')
    const passwordInput = screen.getByPlaceholderText('Masukkan password')
    const submitButton = screen.getByText('Masuk')

    fireEvent.change(usernameInput, { target: { value: 'testuser' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.click(submitButton)

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'password123',
      })
    })
  })
})
```

## 17. Build and Deployment

### 17.1 Docker Configuration
```dockerfile
# Dockerfile
FROM node:18-alpine AS base

# Install dependencies only when needed
FROM base AS deps
RUN apk add --no-cache libc6-compat
WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci

# Rebuild the source code only when needed
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

ENV NEXT_TELEMETRY_DISABLED 1

RUN npm run build

# Production image, copy all the files and run next
FROM base AS runner
WORKDIR /app

ENV NODE_ENV production
ENV NEXT_TELEMETRY_DISABLED 1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public

COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000

ENV PORT 3000
ENV HOSTNAME "0.0.0.0"

CMD ["node", "server.js"]
```

### 17.2 Docker Compose for Full Stack
```yaml
# docker-compose.yml
version: '3.8'

services:
  database:
    image: postgres:15
    environment:
      POSTGRES_DB: bank_syariah_aisyah
      POSTGRES_USER: aisyah_user
      POSTGRES_PASSWORD: aisyah2024!
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - aisyah-network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      DB_USERNAME: aisyah_user
      DB_PASSWORD: aisyah2024!
      JWT_SECRET: aisyahBankSecretKey2024!@#$%^&*()VeryLongSecretKey
      ALLOWED_ORIGINS: http://localhost:3000
    depends_on:
      - database
    networks:
      - aisyah-network

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "3000:3000"
    environment:
      NEXT_PUBLIC_API_BASE_URL: http://backend:8080/api/v1
      NEXT_PUBLIC_APP_NAME: Bank Syariah Aisyah
    depends_on:
      - backend
    networks:
      - aisyah-network

volumes:
  postgres_data:

networks:
  aisyah-network:
    driver: bridge
```

### 17.3 Production Environment Variables
```bash
# .env.production
NEXT_PUBLIC_API_BASE_URL=https://api.aisyahbank.com/api/v1
NEXT_PUBLIC_APP_NAME=Bank Syariah Aisyah
NEXTAUTH_SECRET=your-production-nextauth-secret
NEXTAUTH_URL=https://aisyahbank.com
```

## 18. Performance Optimization

### 18.1 Next.js Configuration for Performance
```javascript
// next.config.js (enhanced)
/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    appDir: true,
  },
  images: {
    domains: ['localhost', 'api.aisyahbank.com'],
    formats: ['image/webp', 'image/avif'],
  },
  compiler: {
    removeConsole: process.env.NODE_ENV === 'production',
  },
  compress: true,
  poweredByHeader: false,
  generateEtags: false,
  httpAgentOptions: {
    keepAlive: true,
  },
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'X-Frame-Options',
            value: 'DENY',
          },
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff',
          },
          {
            key: 'Referrer-Policy',
            value: 'origin-when-cross-origin',
          },
        ],
      },
    ]
  },
}

module.exports = nextConfig
```

### 18.2 Code Splitting and Lazy Loading
```typescript
// src/components/LazyComponents.tsx
import dynamic from 'next/dynamic'
import { Loading } from '@/components/common/Loading'

export const LazyTransactionHistory = dynamic(
  () => import('@/components/transactions/TransactionHistory'),
  {
    loading: () => <Loading />,
    ssr: false,
  }
)

export const LazyAccountChart = dynamic(
  () => import('@/components/accounts/AccountChart'),
  {
    loading: () => <Loading />,
    ssr: false,
  }
)

export const LazyReportGenerator = dynamic(
  () => import('@/components/reports/ReportGenerator'),
  {
    loading: () => <Loading />,
    ssr: false,
  }
)
```

## 19. Security Best Practices

### 19.1 Content Security Policy
```typescript
// middleware.ts
import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

export function middleware(request: NextRequest) {
  const nonce = Buffer.from(crypto.randomUUID()).toString('base64')
  
  const cspHeader = `
    default-src 'self';
    script-src 'self' 'nonce-${nonce}' 'strict-dynamic';
    style-src 'self' 'unsafe-inline';
    img-src 'self' blob: data:;
    font-src 'self';
    object-src 'none';
    base-uri 'self';
    form-action 'self';
    frame-ancestors 'none';
    upgrade-insecure-requests;
  `

  const requestHeaders = new Headers(request.headers)
  requestHeaders.set('x-nonce', nonce)
  requestHeaders.set('Content-Security-Policy', cspHeader.replace(/\s{2,}/g, ' ').trim())

  return NextResponse.next({
    headers: requestHeaders,
    request: {
      headers: requestHeaders,
    },
  })
}
```

### 19.2 Input Sanitization
```typescript
// src/utils/sanitize.ts
import DOMPurify from 'isomorphic-dompurify'

export const sanitizeInput = (input: string): string => {
  return DOMPurify.sanitize(input, { ALLOWED_TAGS: [] })
}

export const sanitizeHtml = (html: string): string => {
  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'u'],
    ALLOWED_ATTR: []
  })
}

export const validateAccountNumber = (accountNumber: string): boolean => {
  const pattern = /^[A-Z]{3}\d{13}$/
  return pattern.test(accountNumber)
}

export const validateAmount = (amount: number): boolean => {
  return amount > 0 && amount <= 1000000000 && Number.isFinite(amount)
}
```

## 20. Accessibility Features

### 20.1 Accessibility Components
```typescript
// src/components/ui/accessible-button.tsx
import * as React from "react"
import { Button, ButtonProps } from "./button"

interface AccessibleButtonProps extends ButtonProps {
  ariaLabel?: string
  loading?: boolean
  loadingText?: string
}

export const AccessibleButton = React.forwardRef<
  HTMLButtonElement,
  AccessibleButtonProps
>(({ ariaLabel, loading, loadingText, children, disabled, ...props }, ref) => {
  return (
    <Button
      ref={ref}
      aria-label={ariaLabel}
      aria-busy={loading}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? (
        <span className="flex items-center">
          <svg className="animate-spin -ml-1 mr-3 h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          {loadingText || 'Loading...'}
        </span>
      ) : (
        children
      )}
    </Button>
  )
})
AccessibleButton.displayName = "AccessibleButton"
```

## 21. Development Commands

### 21.1 Package.json Scripts (Enhanced)
```json
{
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "lint:fix": "next lint --fix",
    "type-check": "tsc --noEmit",
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage",
    "test:e2e": "playwright test",
    "build:analyze": "ANALYZE=true next build",
    "clean": "rm -rf .next out dist",
    "format": "prettier --write .",
    "format:check": "prettier --check .",
    "prepare": "husky install"
  }
}
```

### 21.2 Development Setup Instructions
```bash
# 1. Clone the repository
git clone <repository-url>
cd bank-syariah-aisyah-frontend

# 2. Install dependencies
npm install

# 3. Copy environment file
cp .env.example .env.local

# 4. Configure environment variables
# Edit .env.local with your API endpoint and other configs

# 5. Start development server
npm run dev

# 6. Open browser
# Navigate to http://localhost:3000

# 7. Build for production
npm run build

# 8. Start production server
npm run start

# 9. Run tests
npm run test

# 10. Type checking
npm run type-check

# 11. Linting
npm run lint
```

## 22. Deployment Options

### 22.1 Vercel Deployment
```bash
# Install Vercel CLI
npm install -g vercel

# Login to Vercel
vercel login

# Deploy
vercel

# Set environment variables in Vercel dashboard
# NEXT_PUBLIC_API_BASE_URL
# NEXT_PUBLIC_APP_NAME
# NEXTAUTH_SECRET
# NEXTAUTH_URL
```

### 22.2 AWS Deployment with CDK
```typescript
// infrastructure/frontend-stack.ts
import * as cdk from 'aws-cdk-lib'
import * as s3 from 'aws-cdk-lib/aws-s3'
import * as cloudfront from 'aws-cdk-lib/aws-cloudfront'
import * as origins from 'aws-cdk-lib/aws-cloudfront-origins'

export class FrontendStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props)

    // S3 bucket for static assets
    const bucket = new s3.Bucket(this, 'AisyahBankFrontendBucket', {
      bucketName: 'aisyah-bank-frontend',
      publicReadAccess: true,
      staticWebsiteIndexDocument: 'index.html',
      staticWebsiteErrorDocument: 'error.html',
      removalPolicy: cdk.RemovalPolicy.DESTROY,
    })

    // CloudFront distribution
    const distribution = new cloudfront.Distribution(this, 'AisyahBankDistribution', {
      defaultBehavior: {
        origin: new origins.S3Origin(bucket),
        viewerProtocolPolicy: cloudfront.ViewerProtocolPolicy.REDIRECT_TO_HTTPS,
        cachePolicy: cloudfront.CachePolicy.CACHING_OPTIMIZED,
      },
      defaultRootObject: 'index.html',
      errorResponses: [
        {
          httpStatus: 404,
          responseHttpStatus: 200,
          responsePagePath: '/index.html',
        },
      ],
    })

    // Output the CloudFront URL
    new cdk.CfnOutput(this, 'DistributionUrl', {
      value: distribution.domainName,
    })
  }
}
```

## 23. Monitoring and Analytics

### 23.1 Error Tracking with Sentry
```typescript
// src/lib/sentry.ts
import * as Sentry from '@sentry/nextjs'

Sentry.init({
  dsn: process.env.NEXT_PUBLIC_SENTRY_DSN,
  environment: process.env.NODE_ENV,
  tracesSampleRate: 1.0,
  beforeSend(event) {
    if (event.exception) {
      const error = event.exception.values?.[0]
      console.error('Sentry error:', error)
    }
    return event
  },
})

export default Sentry
```

### 23.2 Performance Monitoring
```typescript
// src/lib/analytics.ts
export const trackEvent = (eventName: string, properties?: Record<string, any>) => {
  if (typeof window !== 'undefined' && window.gtag) {
    window.gtag('event', eventName, {
      event_category: 'banking',
      event_label: eventName,
      ...properties,
    })
  }
}

export const trackPageView = (url: string) => {
  if (typeof window !== 'undefined' && window.gtag) {
    window.gtag('config', process.env.NEXT_PUBLIC_GA_TRACKING_ID, {
      page_location: url,
    })
  }
}

export const trackTransaction = (type: string, amount: number) => {
  trackEvent('transaction', {
    transaction_type: type,
    value: amount,
    currency: 'IDR',
  })
}
```

## 24. Final Notes and Best Practices

### 24.1 Code Quality Guidelines
- Use TypeScript for type safety
- Follow ESLint and Prettier configurations
- Write meaningful commit messages
- Use semantic versioning
- Document complex functions and components
- Implement proper error boundaries
- Use React Query for server state management
- Implement proper loading states
- Use optimistic updates where appropriate
- Follow WCAG guidelines for accessibility

### 24.2 Performance Checklist
- ✅ Code splitting with dynamic imports
- ✅ Image optimization with Next.js Image
- ✅ Bundle analysis with @next/bundle-analyzer
- ✅ Service worker for caching
- ✅ Lazy loading for non-critical components
- ✅ Memoization for expensive calculations
- ✅ Virtual scrolling for large lists
- ✅ Proper key props for React lists
- ✅ Minimize bundle size
- ✅ Use CDN for static assets

### 24.3 Security Checklist
- ✅ Input validation and sanitization
- ✅ XSS protection
- ✅ CSRF protection
- ✅ Secure headers
- ✅ Content Security Policy
- ✅ Secure authentication flow
- ✅ Environment variables for secrets
- ✅ Regular dependency updates
- ✅ Security audits
- ✅ HTTPS enforcement

This comprehensive frontend guide provides a complete foundation for building a modern, secure, and user-friendly Islamic banking application using Next.js with TypeScript, Tailwind CSS, and best practices for performance, security, and accessibility.const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    .min(8, 'Password minimal 8 karakter')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/, 
      'Password harus mengandung huruf besar, kecil, angka, dan karakter khusus'),
  fullName: z.string().min(2, 'Nama lengkap minimal 2 karakter'),
  phoneNumber: z.string().regex(/^(\+62|62|0)8[1-9][0-9]{6,9}$/, 'Format nomor HP tidak valid').optional(),
  dateOfBirth: z.string().optional(),
  nationalId: z.string().regex(/^[0-9]{16}$/, 'NIK harus 16 digit angka').optional(),
  address: z.string().max(500, 'Alamat maksimal 500 karakter').optional(),
})

export default function RegisterForm() {
  const [isLoading, setIsLoading] = useState(false)
  const { register } = useAuth(false)
  const router = useRouter()

  const form = useForm<RegisterRequest>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      fullName: '',
      phoneNumber: '',
      dateOfBirth: '',
      nationalId: '',
      address: '',
    },
  })

  const onSubmit = async (data: RegisterRequest) => {
    setIsLoading(true)
    try {
      await register(data)
      router.push('/login?message=Registration successful')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Registration failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100 py-8">
      <Card className="w-full max-w-2xl">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Daftar Akun Baru
          </CardTitle>
          <CardDescription>
            Bergabunglah dengan Bank Syariah Aisyah
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="username"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Username *</FormLabel>
                      <FormControl>
                        <Input placeholder="Username" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="email"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Email *</FormLabel>
                      <FormControl>
                        <Input type="email" placeholder="Email" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password *</FormLabel>
                    <FormControl>
                      <Input type="password" placeholder="Password" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="fullName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Nama Lengkap *</FormLabel>
                    <FormControl>
                      <Input placeholder="Nama lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="phoneNumber"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Nomor HP</FormLabel>
                      <FormControl>
                        <Input placeholder="08xxxxxxxxx" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                
                <FormField
                  control={form.control}
                  name="dateOfBirth"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Tanggal Lahir</FormLabel>
                      <FormControl>
                        <Input type="date" {...field} disabled={isLoading} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="nationalId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>NIK (Nomor Induk Kependudukan)</FormLabel>
                    <FormControl>
                      <Input placeholder="16 digit NIK" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="address"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Alamat</FormLabel>
                    <FormControl>
                      <Input placeholder="Alamat lengkap" {...field} disabled={isLoading} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Mendaftarkan...' : 'Daftar'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Sudah punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/login')}
            >
              Masuk sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.3 Auth Guard Component
```typescript
// src/components/auth/AuthGuard.tsx
'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'
import { Loading } from '@/components/common/Loading'

interface AuthGuardProps {
  children: React.ReactNode
  requireAuth?: boolean
  requiredRole?: string[]
}

export default function AuthGuard({ 
  children, 
  requireAuth = true, 
  requiredRole 
}: AuthGuardProps) {
  const { isAuthenticated, user, isLoading } = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (!isLoading) {
      if (requireAuth && !isAuthenticated) {
        router.push('/login')
        return
      }

      if (requiredRole && user && !requiredRole.includes(user.role)) {
        router.push('/unauthorized')
        return
      }
    }
  }, [isAuthenticated, user, isLoading, requireAuth, requiredRole, router])

  if (isLoading) {
    return <Loading />
  }

  if (requireAuth && !isAuthenticated) {
    return null
  }

  if (requiredRole && user && !requiredRole.includes(user.role)) {
    return null
  }

  return <>{children}</>
}
```

## 9. Dashboard Components

### 9.1 Dashboard Layout
```typescript
// src/components/dashboard/Sidebar.tsx
'use client'

import { useState } from 'react'
import { usePathname } from 'next/navigation'
import Link from 'next/link'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/hooks/useAuth'
import {
  HomeIcon,
  CreditCardIcon,
  ArrowsRightLeftIcon,
  ChartBarIcon,
  UserIcon,
  Bars3Icon,
  XMarkIcon,
} from '@heroicons/react/24/outline'

const navigation = [
  { name: 'Dashboard', href: '/dashboard', icon: HomeIcon },
  { name: 'Rekening', href: '/dashboard/accounts', icon: CreditCardIcon },
  { name: 'Transaksi', href: '/dashboard/transactions', icon: ArrowsRightLeftIcon },
  { name: 'Laporan', href: '/dashboard/reports', icon: ChartBarIcon },
  { name: 'Profile', href: '/dashboard/profile', icon: UserIcon },
]

export default function Sidebar() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const pathname = usePathname()
  const { user, logout } = useAuth()

  return (
    <>
      {/* Mobile sidebar */}
      <div className={cn(
        'fixed inset-0 flex z-40 md:hidden',
        sidebarOpen ? 'block' : 'hidden'
      )}>
        <div className="fixed inset-0 bg-gray-600 bg-opacity-75" onClick={() => setSidebarOpen(false)} />
        <div className="relative flex-1 flex flex-col max-w-xs w-full bg-white">
          <div className="absolute top-0 right-0 -mr-12 pt-2">
            <Button
              variant="ghost"
              onClick={() => setSidebarOpen(false)}
              className="ml-1 flex items-center justify-center h-10 w-10 rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
            >
              <XMarkIcon className="h-6 w-6 text-white" />
            </Button>
          </div>
          <SidebarContent />
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className="hidden md:flex md:w-64 md:flex-col md:fixed md:inset-y-0">
        <div className="flex-1 flex flex-col min-h-0 border-r border-gray-200 bg-white">
          <SidebarContent />
        </div>
      </div>

      {/* Mobile menu button */}
      <div className="sticky top-0 z-10 md:hidden pl-1 pt-1 sm:pl-3 sm:pt-3 bg-white border-b">
        <Button
          variant="ghost"
          onClick={() => setSidebarOpen(true)}
          className="-ml-0.5 -mt-0.5 h-12 w-12 inline-flex items-center justify-center"
        >
          <Bars3Icon className="h-6 w-6" />
        </Button>
      </div>
    </>
  )

  function SidebarContent() {
    return (
      <>
        <div className="flex-1 flex flex-col pt-5 pb-4 overflow-y-auto">
          <div className="flex items-center flex-shrink-0 px-4">
            <h2 className="text-lg font-semibold text-emerald-800">
              Bank Syariah Aisyah
            </h2>
          </div>
          <nav className="mt-5 flex-1 px-2 space-y-1">
            {navigation.map((item) => {
              const isActive = pathname === item.href
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={cn(
                    'group flex items-center px-2 py-2 text-sm font-medium rounded-md',
                    isActive
                      ? 'bg-emerald-100 text-emerald-900'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  )}
                >
                  <item.icon
                    className={cn(
                      'mr-3 flex-shrink-0 h-6 w-6',
                      isActive ? 'text-emerald-500' : 'text-gray-400 group-hover:text-gray-500'
                    )}
                  />
                  {item.name}
                </Link>
              )
            })}
          </nav>
        </div>
        
        {/* User info and logout */}
        <div className="flex-shrink-0 flex border-t border-gray-200 p-4">
          <div className="flex-shrink-0 w-full group block">
            <div className="flex items-center">
              <div>
                <div className="inline-flex items-center justify-center h-9 w-9 rounded-full bg-emerald-500">
                  <span className="text-sm font-medium leading-none text-white">
                    {user?.fullName?.charAt(0).toUpperCase()}
                  </span>
                </div>
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-700">{user?.fullName}</p>
                <Button
                  variant="link"
                  onClick={logout}
                  className="text-xs text-gray-500 p-0 h-auto"
                >
                  Keluar
                </Button>
              </div>
            </div>
          </div>
        </div>
      </>
    )
  }
}
```

### 9.2 Dashboard Header
```typescript
// src/components/dashboard/Header.tsx
'use client'

import { Bell, Search } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

interface HeaderProps {
  title: string
  description?: string
}

export default function Header({ title, description }: HeaderProps) {
  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center py-6">
          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold leading-7 text-gray-900 sm:truncate">
              {title}
            </h1>
            {description && (
              <p className="mt-1 text-sm text-gray-500">
                {description}
              </p>
            )}
          </div>
          
          <div className="flex items-center space-x-4">
            <div className="flex-1 max-w-lg">
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Search className="h-5 w-5 text-gray-400" />
                </div>
                <Input
                  type="search"
                  placeholder="Cari..."
                  className="pl-10 pr-4 py-2 w-full"
                />
              </div>
            </div>
            
            <Button variant="ghost" size="icon">
              <Bell className="h-5 w-5" />
            </Button>
          </div>
        </div>
      </div>
    </header>
  )
}
```

### 9.3 Statistics Cards
```typescript
// src/components/dashboard/StatCards.tsx
'use client'

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { formatCurrency } from '@/utils/formatters'
import { 
  CreditCardIcon, 
  ArrowUpIcon, 
  ArrowDownIcon,
  BanknotesIcon 
} from '@heroicons/react/24/outline'

interface StatCard {
  title: string
  value: string
  change?: string
  changeType?: 'increase' | 'decrease'
  icon: React.ComponentType<any>
}

interface StatCardsProps {
  totalBalance: number
  totalAccounts: number
  monthlyIncome: number
  monthlyExpense: number
}

export default function StatCards({ 
  totalBalance, 
  totalAccounts, 
  monthlyIncome, 
  monthlyExpense 
}: StatCardsProps) {
  const stats: StatCard[] = [
    {
      title: 'Total Saldo',
      value: formatCurrency(totalBalance),
      icon: BanknotesIcon,
    },
    {
      title: 'Jumlah Rekening',
      value: totalAccounts.toString(),
      icon: CreditCardIcon,
    },
    {
      title: 'Pemasukan Bulan Ini',
      value: formatCurrency(monthlyIncome),
      change: '+12%',
      changeType: 'increase',
      icon: ArrowUpIcon,
    },
    {
      title: 'Pengeluaran Bulan Ini',
      value: formatCurrency(monthlyExpense),
      change: '-5%',
      changeType: 'decrease',
      icon: ArrowDownIcon,
    },
  ]

  return (
    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
      {stats.map((stat) => (
        <Card key={stat.title}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {stat.title}
            </CardTitle>
            <stat.icon className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stat.value}</div>
            {stat.change && (
              <p className={`text-xs ${
                stat.changeType === 'increase' ? 'text-green-600' : 'text-red-600'
              }`}>
                {stat.change} dari bulan lalu
              </p>
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
```

### 9.4 Recent Transactions Component
```typescript
// src/components/dashboard/RecentTransactions.tsx
'use client'

import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Transaction } from '@/types/transaction'
import { apiService } from '@/lib/api'
import { formatCurrency, formatDate } from '@/utils/formatters'
import { ArrowUpIcon, ArrowDownIcon } from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function RecentTransactions() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchRecentTransactions = async () => {
      try {
        const response = await apiService.get<Transaction[]>('/transactions/recent?limit=5')
        setTransactions(response)
      } catch (error) {
        console.error('Failed to fetch recent transactions:', error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchRecentTransactions()
  }, [])

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Transaksi Terbaru</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="flex items-center space-x-4">
                  <div className="w-10 h-10 bg-gray-200 rounded-full"></div>
                  <div className="flex-1 space-y-2">
                    <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                    <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                  </div>
                  <div className="h-4 bg-gray-200 rounded w-20"></div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <div>
          <CardTitle>Transaksi Terbaru</CardTitle>
          <CardDescription>
            {transactions.length} transaksi terakhir
          </CardDescription>
        </div>
        <Button asChild variant="outline" size="sm">
          <Link href="/dashboard/transactions">
            Lihat Semua
          </Link>
        </Button>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {transactions.map((transaction) => (
            <div key={transaction.id} className="flex items-center space-x-4">
              <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
                transaction.transactionType === 'CREDIT' 
                  ? 'bg-green-100' 
                  : 'bg-red-100'
              }`}>
                {transaction.transactionType === 'CREDIT' ? (
                  <ArrowUpIcon className="w-5 h-5 text-green-600" />
                ) : (
                  <ArrowDownIcon className="w-5 h-5 text-red-600" />
                )}
              </div>
              
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 truncate">
                  {transaction.description}
                </p>
                <p className="text-sm text-gray-500">
                  {formatDate(transaction.transactionDate)} • {transaction.accountNumber}
                </p>
              </div>
              
              <div className="text-right">
                <p className={`text-sm font-medium ${
                  transaction.transactionType === 'CREDIT' 
                    ? 'text-green-600' 
                    : 'text-red-600'
                }`}>
                  {transaction.transactionType === 'CREDIT' ? '+' : '-'}
                  {formatCurrency(transaction.amount)}
                </p>
                <p className="text-xs text-gray-500">
                  {transaction.status}
                </p>
              </div>
            </div>
          ))}
          
          {transactions.length === 0 && (
            <div className="text-center py-6">
              <p className="text-gray-500">Belum ada transaksi</p>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
```

## 10. Account Management Components

### 10.1 Account List
```typescript
// src/components/accounts/AccountList.tsx
'use client'

import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Account } from '@/types/account'
import { formatCurrency } from '@/utils/formatters'
import { useAccounts } from '@/hooks/useAccounts'
import { 
  CreditCardIcon, 
  EyeIcon, 
  PlusIcon 
} from '@heroicons/react/24/outline'
import Link from 'next/link'

export default function AccountList() {
  const { accounts, isLoading } = useAccounts()
  const [visibleBalances, setVisibleBalances] = useState<Record<number, boolean>>({})

  const toggleBalanceVisibility = (accountId: number) => {
    setVisibleBalances(prev => ({
      ...prev,
      [accountId]: !prev[accountId]
    }))
  }

  const getStatusBadge = (status: string) => {
    const statusConfig = {
      ACTIVE: { label: 'Aktif', variant: 'default' as const },
      SUSPENDED: { label: 'Ditangguhkan', variant: 'secondary' as const },
      CLOSED: { label: 'Ditutup', variant: 'destructive' as const },
      FROZEN: { label: 'Dibekukan', variant: 'outline' as const },
    }
    
    return statusConfig[status as keyof typeof statusConfig] || {
      label: status,
      variant: 'outline' as const
    }
  }

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Array.from({ length: 3 }).map((_, i) => (
          <Card key={i} className="animate-pulse">
            <CardHeader>
              <div className="h-4 bg-gray-200 rounded w-3/4"></div>
              <div className="h-3 bg-gray-200 rounded w-1/2"></div>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="h-6 bg-gray-200 rounded w-full"></div>
                <div className="h-4 bg-gray-200 rounded w-2/3"></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold">Rekening Saya</h2>
        <Button asChild>
          <Link href="/dashboard/accounts/create">
            <PlusIcon className="w-4 h-4 mr-2" />
            Buka Rekening Baru
          </Link>
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {accounts.map((account) => {
          const statusBadge = getStatusBadge(account.status)
          const isBalanceVisible = visibleBalances[account.id]
          
          return (
            <Card key={account.id} className="hover:shadow-md transition-shadow">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="p-2 bg-emerald-100 rounded-lg">
                      <CreditCardIcon className="w-5 h-5 text-emerald-600" />
                    </div>
                    <div>
                      <CardTitle className="text-sm font-medium">
                        {account.accountName}
                      </CardTitle>
                      <p className="text-xs text-gray-500">
                        {account.accountNumber}
                      </p>
                    </div>
                  </div>
                  <Badge variant={statusBadge.variant}>
                    {statusBadge.label}
                  </Badge>
                </div>
              </CardHeader>
              
              <CardContent className="pt-0">
                <div className="space-y-3">
                  <div>
                    <p className="text-xs text-gray-500 mb-1">Saldo</p>
                    <div className="flex items-center justify-between">
                      <p className="text-lg font-bold">
                        {isBalanceVisible 
                          ? formatCurrency(account.balance)
                          : '••••••••'
                        }
                      </p>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => toggleBalanceVisibility(account.id)}
                      >
                        <EyeIcon className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  
                  <div className="flex justify-between text-xs text-gray-500">
                    <span>Jenis: {account.accountTypeName}</span>
                    <span>{account.currency}</span>
                  </div>
                  
                  <div className="flex space-x-2">
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/accounts/${account.id}`}>
                        Detail
                      </Link>
                    </Button>
                    <Button asChild variant="outline" size="sm" className="flex-1">
                      <Link href={`/dashboard/transactions?account=${account.accountNumber}`}>
                        Transaksi
                      </Link>
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
        
        {accounts.length === 0 && (
          <Card className="col-span-full">
            <CardContent className="text-center py-12">
              <CreditCardIcon className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                Belum ada rekening
              </h3>
              <p className="text-gray-500 mb-6">
                Buka rekening pertama Anda untuk mulai bertransaksi
              </p>
              <Button asChild>
                <Link href="/dashboard/accounts/create">
                  Buka Rekening Sekarang
                </Link>
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
```

## 11. Transaction Components

### 11.1 Transfer Form
```typescript
// src/components/transactions/TransferForm.tsx
'use client'

import { useState } from 'react'
import { useForm# Frontend Guide - Bank Syariah Aisyah Application (Next.js)

## 1. Project Setup

### 1.1 Initialize Next.js Project
```bash
npx create-next-app@latest bank-syariah-aisyah-frontend --typescript --tailwind --eslint --app
cd bank-syariah-aisyah-frontend
```

### 1.2 Package.json Configuration
```json
{
  "name": "bank-syariah-aisyah-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "type-check": "tsc --noEmit",
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage"
  },
  "dependencies": {
    "@headlessui/react": "^1.7.17",
    "@heroicons/react": "^2.0.18",
    "@hookform/resolvers": "^3.3.2",
    "@radix-ui/react-avatar": "^1.0.4",
    "@radix-ui/react-dialog": "^1.0.5",
    "@radix-ui/react-dropdown-menu": "^2.0.6",
    "@radix-ui/react-label": "^2.0.2",
    "@radix-ui/react-popover": "^1.0.7",
    "@radix-ui/react-select": "^2.0.0",
    "@radix-ui/react-slot": "^1.0.2",
    "@radix-ui/react-tabs": "^1.0.4",
    "@radix-ui/react-toast": "^1.1.5",
    "@tanstack/react-query": "^5.8.4",
    "@types/node": "^20",
    "@types/react": "^18",
    "@types/react-dom": "^18",
    "autoprefixer": "^10",
    "axios": "^1.6.2",
    "class-variance-authority": "^0.7.0",
    "clsx": "^2.0.0",
    "date-fns": "^2.30.0",
    "framer-motion": "^10.16.5",
    "js-cookie": "^3.0.5",
    "lucide-react": "^0.294.0",
    "next": "14.0.3",
    "next-auth": "^4.24.5",
    "next-themes": "^0.2.1",
    "react": "^18",
    "react-dom": "^18",
    "react-hook-form": "^7.48.2",
    "recharts": "^2.8.0",
    "tailwind-merge": "^2.0.0",
    "tailwindcss-animate": "^1.0.7",
    "typescript": "^5",
    "zod": "^3.22.4",
    "zustand": "^4.4.7"
  },
  "devDependencies": {
    "@types/js-cookie": "^3.0.6",
    "@types/jest": "^29.5.8",
    "eslint": "^8",
    "eslint-config-next": "14.0.3",
    "jest": "^29.7.0",
    "jest-environment-jsdom": "^29.7.0",
    "postcss": "^8",
    "tailwindcss": "^3.3.6"
  }
}
```

### 1.3 Next.js Configuration
```typescript
// next.config.js
/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    appDir: true,
  },
  images: {
    domains: ['localhost', 'api.aisyahbank.com'],
  },
  env: {
    NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL,
    NEXT_PUBLIC_APP_NAME: 'Bank Syariah Aisyah',
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${process.env.NEXT_PUBLIC_API_BASE_URL}/:path*`,
      },
    ]
  },
}

module.exports = nextConfig
```

### 1.4 Environment Variables
```bash
# .env.local
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_APP_NAME=Bank Syariah Aisyah
NEXTAUTH_SECRET=your-nextauth-secret
NEXTAUTH_URL=http://localhost:3000
```

## 2. Project Structure

```
src/
├── app/
│   ├── (auth)/
│   │   ├── login/
│   │   │   └── page.tsx
│   │   ├── register/
│   │   │   └── page.tsx
│   │   └── layout.tsx
│   ├── (dashboard)/
│   │   ├── accounts/
│   │   │   ├── page.tsx
│   │   │   ├── create/
│   │   │   │   └── page.tsx
│   │   │   └── [id]/
│   │   │       └── page.tsx
│   │   ├── transactions/
│   │   │   ├── page.tsx
│   │   │   ├── transfer/
│   │   │   │   └── page.tsx
│   │   │   ├── deposit/
│   │   │   │   └── page.tsx
│   │   │   └── withdrawal/
│   │   │       └── page.tsx
│   │   ├── reports/
│   │   │   └── page.tsx
│   │   ├── profile/
│   │   │   └── page.tsx
│   │   ├── page.tsx
│   │   └── layout.tsx
│   ├── globals.css
│   ├── layout.tsx
│   ├── loading.tsx
│   ├── error.tsx
│   └── not-found.tsx
├── components/
│   ├── ui/
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── dialog.tsx
│   │   ├── form.tsx
│   │   ├── input.tsx
│   │   ├── label.tsx
│   │   ├── select.tsx
│   │   ├── table.tsx
│   │   ├── tabs.tsx
│   │   └── toast.tsx
│   ├── auth/
│   │   ├── LoginForm.tsx
│   │   ├── RegisterForm.tsx
│   │   └── AuthGuard.tsx
│   ├── dashboard/
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   ├── StatCards.tsx
│   │   └── RecentTransactions.tsx
│   ├── accounts/
│   │   ├── AccountCard.tsx
│   │   ├── AccountList.tsx
│   │   └── CreateAccountForm.tsx
│   ├── transactions/
│   │   ├── TransactionList.tsx
│   │   ├── TransactionForm.tsx
│   │   ├── TransferForm.tsx
│   │   ├── DepositForm.tsx
│   │   └── WithdrawalForm.tsx
│   └── common/
│       ├── Loading.tsx
│       ├── ErrorBoundary.tsx
│       └── Layout.tsx
├── lib/
│   ├── api.ts
│   ├── auth.ts
│   ├── utils.ts
│   ├── validations.ts
│   └── constants.ts
├── hooks/
│   ├── useAuth.ts
│   ├── useAccounts.ts
│   ├── useTransactions.ts
│   ├── useLocalStorage.ts
│   └── useDebounce.ts
├── store/
│   ├── authStore.ts
│   ├── accountStore.ts
│   └── transactionStore.ts
├── types/
│   ├── auth.ts
│   ├── account.ts
│   ├── transaction.ts
│   └── api.ts
└── utils/
    ├── formatters.ts
    ├── validators.ts
    └── helpers.ts
```

## 3. UI Components with shadcn/ui

### 3.1 Install shadcn/ui
```bash
npx shadcn-ui@latest init
npx shadcn-ui@latest add button card dialog form input label select table tabs toast
```

### 3.2 Button Component
```typescript
// src/components/ui/button.tsx
import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "@/lib/utils"

const buttonVariants = cva(
  "inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
        outline: "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
        secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline",
        islamic: "bg-emerald-600 text-white hover:bg-emerald-700",
      },
      size: {
        default: "h-10 px-4 py-2",
        sm: "h-9 rounded-md px-3",
        lg: "h-11 rounded-md px-8",
        icon: "h-10 w-10",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button"
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    )
  }
)
Button.displayName = "Button"

export { Button, buttonVariants }
```

### 3.3 Card Component
```typescript
// src/components/ui/card.tsx
import * as React from "react"
import { cn } from "@/lib/utils"

const Card = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn(
      "rounded-lg border bg-card text-card-foreground shadow-sm",
      className
    )}
    {...props}
  />
))
Card.displayName = "Card"

const CardHeader = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex flex-col space-y-1.5 p-6", className)}
    {...props}
  />
))
CardHeader.displayName = "CardHeader"

const CardTitle = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLHeadingElement>
>(({ className, ...props }, ref) => (
  <h3
    ref={ref}
    className={cn(
      "text-2xl font-semibold leading-none tracking-tight",
      className
    )}
    {...props}
  />
))
CardTitle.displayName = "CardTitle"

const CardDescription = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => (
  <p
    ref={ref}
    className={cn("text-sm text-muted-foreground", className)}
    {...props}
  />
))
CardDescription.displayName = "CardDescription"

const CardContent = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div ref={ref} className={cn("p-6 pt-0", className)} {...props} />
))
CardContent.displayName = "CardContent"

const CardFooter = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex items-center p-6 pt-0", className)}
    {...props}
  />
))
CardFooter.displayName = "CardFooter"

export { Card, CardHeader, CardFooter, CardTitle, CardDescription, CardContent }
```

## 4. Type Definitions

### 4.1 Authentication Types
```typescript
// src/types/auth.ts
export interface User {
  id: number
  username: string
  email: string
  fullName: string
  role: 'ADMIN' | 'MANAGER' | 'TELLER' | 'CUSTOMER'
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
  emailVerified: boolean
  phoneVerified: boolean
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  fullName: string
  phoneNumber?: string
  dateOfBirth?: string
  nationalId?: string
  address?: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface AuthState {
  user: User | null
  token: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoading: boolean
}
```

### 4.2 Account Types
```typescript
// src/types/account.ts
export interface Account {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  accountTypeCode: string
  balance: number
  availableBalance: number
  currency: string
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED' | 'FROZEN'
  openedDate: string
  closedDate?: string
  customerNumber: string
  customerName: string
  profitSharingPercentage?: number
  isDormant: boolean
  lastTransactionDate?: string
}

export interface AccountType {
  id: number
  typeCode: string
  typeName: string
  description: string
  minimumBalance: number
  maintenanceFee: number
  profitSharingRatio?: number
  isSavings: boolean
  isActive: boolean
}

export interface CreateAccountRequest {
  accountTypeId: number
  accountName: string
  initialDeposit: number
  purpose?: string
}

export interface AccountResponse {
  id: number
  accountNumber: string
  accountName: string
  accountTypeName: string
  balance: number
  status: string
  openedDate: string
}
```

### 4.3 Transaction Types
```typescript
// src/types/transaction.ts
export interface Transaction {
  id: number
  transactionNumber: string
  accountNumber: string
  accountName: string
  categoryName: string
  amount: number
  transactionType: 'CREDIT' | 'DEBIT'
  description: string
  balanceBefore: number
  balanceAfter: number
  transactionDate: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REVERSED'
  channel: 'TELLER' | 'ATM' | 'MOBILE' | 'WEB'
  toAccountNumber?: string
  toAccountName?: string
}

export interface DepositRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface WithdrawalRequest {
  accountNumber: string
  amount: number
  description: string
}

export interface TransferRequest {
  fromAccountNumber: string
  toAccountNumber: string
  amount: number
  description: string
}

export interface TransactionResponse {
  id: number
  transactionNumber: string
  amount: number
  description: string
  transactionDate: string
  status: string
  balanceAfter: number
}
```

## 5. API Service Layer

### 5.1 API Configuration
```typescript
// src/lib/api.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { getToken, removeTokens } from './auth'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1'

class ApiService {
  private api: AxiosInstance

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 10000,
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    // Request interceptor to add auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = getToken()
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Response interceptor to handle auth errors
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          removeTokens()
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }
    )
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.get(url, config)
    return response.data
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.post(url, data, config)
    return response.data
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.put(url, data, config)
    return response.data
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.api.delete(url, config)
    return response.data
  }
}

export const apiService = new ApiService()
```

### 5.2 Authentication Service
```typescript
// src/lib/auth.ts
import { apiService } from './api'
import { LoginRequest, RegisterRequest, LoginResponse } from '@/types/auth'
import Cookies from 'js-cookie'

const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEY = 'user'

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiService.post<LoginResponse>('/auth/login', credentials)
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  async register(userData: RegisterRequest): Promise<void> {
    await apiService.post('/auth/register', userData)
  },

  async refreshToken(): Promise<LoginResponse> {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      throw new Error('No refresh token available')
    }
    
    const response = await apiService.post<LoginResponse>('/auth/refresh', { refreshToken })
    
    if (response.token) {
      setTokens(response.token, response.refreshToken)
      setUser(response.user)
    }
    
    return response
  },

  logout() {
    removeTokens()
    removeUser()
  }
}

// Token management functions
export const setTokens = (token: string, refreshToken: string) => {
  Cookies.set(TOKEN_KEY, token, { expires: 1 }) // 1 day
  Cookies.set(REFRESH_TOKEN_KEY, refreshToken, { expires: 7 }) // 7 days
}

export const getToken = (): string | undefined => {
  return Cookies.get(TOKEN_KEY)
}

export const getRefreshToken = (): string | undefined => {
  return Cookies.get(REFRESH_TOKEN_KEY)
}

export const removeTokens = () => {
  Cookies.remove(TOKEN_KEY)
  Cookies.remove(REFRESH_TOKEN_KEY)
}

export const setUser = (user: any) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const getUser = () => {
  if (typeof window === 'undefined') return null
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}

export const removeUser = () => {
  localStorage.removeItem(USER_KEY)
}

export const isAuthenticated = (): boolean => {
  return !!getToken()
}
```

## 6. State Management with Zustand

### 6.1 Auth Store
```typescript
// src/store/authStore.ts
import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'
import { User, LoginRequest, RegisterRequest, AuthState } from '@/types/auth'
import { authService } from '@/lib/auth'

interface AuthStore extends AuthState {
  login: (credentials: LoginRequest) => Promise<void>
  register: (userData: RegisterRequest) => Promise<void>
  logout: () => void
  refreshToken: () => Promise<void>
  setUser: (user: User) => void
  setToken: (token: string, refreshToken: string) => void
  clearAuth: () => void
}

export const useAuthStore = create<AuthStore>()(
  devtools(
    persist(
      (set, get) => ({
        user: null,
        token: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,

        login: async (credentials: LoginRequest) => {
          set({ isLoading: true })
          try {
            const response = await authService.login(credentials)
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
              isLoading: false,
            })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        register: async (userData: RegisterRequest) => {
          set({ isLoading: true })
          try {
            await authService.register(userData)
            set({ isLoading: false })
          } catch (error) {
            set({ isLoading: false })
            throw error
          }
        },

        logout: () => {
          authService.logout()
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },

        refreshToken: async () => {
          try {
            const response = await authService.refreshToken()
            set({
              user: response.user,
              token: response.token,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
            })
          } catch (error) {
            get().logout()
            throw error
          }
        },

        setUser: (user: User) => {
          set({ user })
        },

        setToken: (token: string, refreshToken: string) => {
          set({ token, refreshToken, isAuthenticated: true })
        },

        clearAuth: () => {
          set({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
          })
        },
      }),
      {
        name: 'auth-storage',
        partialize: (state) => ({
          user: state.user,
          token: state.token,
          refreshToken: state.refreshToken,
          isAuthenticated: state.isAuthenticated,
        }),
      }
    )
  )
)
```

### 6.2 Account Store
```typescript
// src/store/accountStore.ts
import { create } from 'zustand'
import { devtools } from 'zustand/middleware'
import { Account, AccountType, CreateAccountRequest } from '@/types/account'
import { apiService } from '@/lib/api'

interface AccountState {
  accounts: Account[]
  accountTypes: AccountType[]
  selectedAccount: Account | null
  isLoading: boolean
  error: string | null
}

interface AccountActions {
  fetchAccounts: () => Promise<void>
  fetchAccountTypes: () => Promise<void>
  createAccount: (request: CreateAccountRequest) => Promise<void>
  getAccountById: (id: number) => Promise<void>
  getAccountByNumber: (accountNumber: string) => Promise<Account>
  setSelectedAccount: (account: Account | null) => void
  clearError: () => void
}

type AccountStore = AccountState & AccountActions

export const useAccountStore = create<AccountStore>()(
  devtools(
    (set, get) => ({
      accounts: [],
      accountTypes: [],
      selectedAccount: null,
      isLoading: false,
      error: null,

      fetchAccounts: async () => {
        set({ isLoading: true, error: null })
        try {
          const accounts = await apiService.get<Account[]>('/accounts/my-accounts')
          set({ accounts, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch accounts',
            isLoading: false 
          })
        }
      },

      fetchAccountTypes: async () => {
        try {
          const accountTypes = await apiService.get<AccountType[]>('/account-types')
          set({ accountTypes })
        } catch (error: any) {
          set({ error: error.response?.data?.message || 'Failed to fetch account types' })
        }
      },

      createAccount: async (request: CreateAccountRequest) => {
        set({ isLoading: true, error: null })
        try {
          const newAccount = await apiService.post<Account>('/accounts', request)
          set(state => ({ 
            accounts: [...state.accounts, newAccount],
            isLoading: false 
          }))
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to create account',
            isLoading: false 
          })
          throw error
        }
      },

      getAccountById: async (id: number) => {
        set({ isLoading: true, error: null })
        try {
          const account = await apiService.get<Account>(`/accounts/${id}`)
          set({ selectedAccount: account, isLoading: false })
        } catch (error: any) {
          set({ 
            error: error.response?.data?.message || 'Failed to fetch account',
            isLoading: false 
          })
        }
      },

      getAccountByNumber: async (accountNumber: string) => {
        const response = await apiService.get<Account>(`/accounts/number/${accountNumber}`)
        return response
      },

      setSelectedAccount: (account: Account | null) => {
        set({ selectedAccount: account })
      },

      clearError: () => {
        set({ error: null })
      },
    })
  )
)
```

## 7. Custom Hooks

### 7.1 useAuth Hook
```typescript
// src/hooks/useAuth.ts
import { useAuthStore } from '@/store/authStore'
import { useRouter } from 'next/navigation'
import { useEffect } from 'react'

export const useAuth = (requireAuth = true) => {
  const authStore = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (requireAuth && !authStore.isAuthenticated) {
      router.push('/login')
    }
  }, [authStore.isAuthenticated, requireAuth, router])

  return {
    user: authStore.user,
    isAuthenticated: authStore.isAuthenticated,
    isLoading: authStore.isLoading,
    login: authStore.login,
    register: authStore.register,
    logout: authStore.logout,
    refreshToken: authStore.refreshToken,
  }
}
```

### 7.2 useAccounts Hook
```typescript
// src/hooks/useAccounts.ts
import { useAccountStore } from '@/store/accountStore'
import { useEffect } from 'react'

export const useAccounts = () => {
  const accountStore = useAccountStore()

  useEffect(() => {
    accountStore.fetchAccounts()
    accountStore.fetchAccountTypes()
  }, [])

  return {
    accounts: accountStore.accounts,
    accountTypes: accountStore.accountTypes,
    selectedAccount: accountStore.selectedAccount,
    isLoading: accountStore.isLoading,
    error: accountStore.error,
    createAccount: accountStore.createAccount,
    getAccountById: accountStore.getAccountById,
    getAccountByNumber: accountStore.getAccountByNumber,
    setSelectedAccount: accountStore.setSelectedAccount,
    clearError: accountStore.clearError,
  }
}
```

### 7.3 useTransactions Hook
```typescript
// src/hooks/useTransactions.ts
import { useState, useEffect } from 'react'
import { Transaction, DepositRequest, WithdrawalRequest, TransferRequest } from '@/types/transaction'
import { apiService } from '@/lib/api'

export const useTransactions = (accountId?: number) => {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchTransactions = async (accountId?: number) => {
    setIsLoading(true)
    setError(null)
    try {
      const url = accountId ? `/transactions/account/${accountId}` : '/transactions/my-transactions'
      const response = await apiService.get<Transaction[]>(url)
      setTransactions(response)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch transactions')
    } finally {
      setIsLoading(false)
    }
  }

  const deposit = async (request: DepositRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/deposit', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process deposit')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const withdraw = async (request: WithdrawalRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/withdrawal', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process withdrawal')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const transfer = async (request: TransferRequest) => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await apiService.post('/transactions/transfer', request)
      await fetchTransactions(accountId)
      return response
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process transfer')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchTransactions(accountId)
  }, [accountId])

  return {
    transactions,
    isLoading,
    error,
    fetchTransactions,
    deposit,
    withdraw,
    transfer,
  }
}
```

## 8. Authentication Components

### 8.1 Login Form
```typescript
// src/components/auth/LoginForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { LoginRequest } from '@/types/auth'
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/24/outline'

const loginSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

export default function LoginForm() {
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const { login } = useAuth(false)
  const router = useRouter()

  const form = useForm<LoginRequest>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  })

  const onSubmit = async (data: LoginRequest) => {
    setIsLoading(true)
    try {
      await login(data)
      router.push('/dashboard')
    } catch (error: any) {
      form.setError('root', {
        message: error.response?.data?.message || 'Login failed',
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-emerald-800">
            Bank Syariah Aisyah
          </CardTitle>
          <CardDescription>
            Masuk ke akun perbankan syariah Anda
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Username</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Masukkan username"
                        {...field}
                        disabled={isLoading}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password</FormLabel>
                    <FormControl>
                      <div className="relative">
                        <Input
                          type={showPassword ? 'text' : 'password'}
                          placeholder="Masukkan password"
                          {...field}
                          disabled={isLoading}
                        />
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                          onClick={() => setShowPassword(!showPassword)}
                          disabled={isLoading}
                        >
                          {showPassword ? (
                            <EyeSlashIcon className="h-4 w-4" />
                          ) : (
                            <EyeIcon className="h-4 w-4" />
                          )}
                        </Button>
                      </div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <div className="text-sm text-red-600 text-center">
                  {form.formState.errors.root.message}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                variant="islamic"
                disabled={isLoading}
              >
                {isLoading ? 'Memproses...' : 'Masuk'}
              </Button>
            </form>
          </Form>
          
          <div className="mt-4 text-center text-sm">
            <span className="text-gray-600">Belum punya akun? </span>
            <Button
              variant="link"
              className="p-0 h-auto font-normal"
              onClick={() => router.push('/register')}
            >
              Daftar sekarang
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

### 8.2 Register Form
```typescript
// src/components/auth/RegisterForm.tsx
'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuth } from '@/hooks/useAuth'
import { RegisterRequest } from '@/types/auth'

const registerSchema = z.object({
  username: z.string().min(3, 'Username minimal 3 karakter'),
  email: z.string().email('Format email tidak valid'),
  password: z
    .string()
    