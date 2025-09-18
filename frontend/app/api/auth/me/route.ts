import { NextRequest, NextResponse } from 'next/server'

export async function GET(request: NextRequest) {
  try {
    const authHeader = request.headers.get('authorization')
    
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return NextResponse.json({
        success: false,
        message: 'No token provided'
      }, { status: 401 })
    }

    const token = authHeader.substring(7)
    
    // Demo token validation
    if (token === 'demo-jwt-token-12345') {
      return NextResponse.json({
        id: '1',
        email: 'admin@company.com',
        name: 'Admin User',
        companyId: '1',
        role: 'ADMIN'
      })
    } else {
      return NextResponse.json({
        success: false,
        message: 'Invalid token'
      }, { status: 401 })
    }
  } catch (error) {
    return NextResponse.json({
      success: false,
      message: 'Invalid request'
    }, { status: 400 })
  }
}
