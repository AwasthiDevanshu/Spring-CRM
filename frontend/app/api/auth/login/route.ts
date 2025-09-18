import { NextRequest, NextResponse } from 'next/server'

export async function POST(request: NextRequest) {
  try {
    const body = await request.json()
    const { email, password } = body

    // Demo credentials
    if (email === 'admin@company.com' && password === 'admin123') {
      return NextResponse.json({
        success: true,
        token: 'demo-jwt-token-12345',
        user: {
          id: '1',
          email: email,
          name: 'Admin User',
          companyId: '1',
          role: 'ADMIN'
        }
      })
    } else {
      return NextResponse.json({
        success: false,
        message: 'Invalid credentials'
      }, { status: 401 })
    }
  } catch (error) {
    return NextResponse.json({
      success: false,
      message: 'Invalid request'
    }, { status: 400 })
  }
}
