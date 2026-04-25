package com.ABC.ABC_FComplaintWebapp.util;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

/**
 * SECURITY FIX_002: Output Sanitization & HTML Escaping Utility
 * Weakness ID: Wk_002
 * Fix ID: Fix_002 – Output Transformation Validation (OTV) with HTML Escaping
 * STRIDE: Tampering, Information Disclosure
 * OWASP: A03 Injection, A07 Cross-Site Scripting (XSS)
 * CWE: CWE-79, CWE-80
 * CIA: Integrity, Confidentiality
 * ASVS: V5.3 – Output Encoding & V5.1 – Input Validation
 * D3FEND: D3-OTV Output Transformation Validation
 *
 * Implementation Details:
 * - Context-appropriate output encoding for HTML, JavaScript, URL, and CSS contexts
 * - Prevents XSS attacks by escaping special characters
 * - Applied to all complaint fields before rendering in web views
 * - Used in both template rendering and administrative interfaces
 */
@Component
public class OutputSanitizer {

    /**
     * HTML escape - safe for HTML content
     * Escapes: <, >, &, ", '
     */
    public static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return StringEscapeUtils.escapeHtml4(input);
    }

    /**
     * JavaScript escape - safe for JavaScript context
     * Escapes special characters that could break out of strings in JS
     */
    public static String escapeJavaScript(String input) {
        if (input == null) {
            return "";
        }
        return StringEscapeUtils.escapeEcmaScript(input);
    }

    /**
     * URL escape - safe for URL parameters and href attributes
     * Percent-encodes special characters
     */
    public static String escapeUrl(String input) {
        if (input == null) {
            return "";
        }
        // Use standard URL encoding from Java
        try {
            return java.net.URLEncoder.encode(input, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return input;
        }
    }

    /**
     * CSS escape - safe for CSS context
     * Escapes special CSS characters
     */
    public static String escapeCss(String input) {
        if (input == null) {
            return "";
        }
        // Basic CSS escaping - escape quotes and dangerous characters
        return input.replaceAll("['\"]", "\\\\$0")
                   .replaceAll("[<>\\\\]", "\\\\$0");
    }

    /**
     * XML escape - safe for XML/SOAP context
     * Escapes: <, >, &, ", '
     */
    public static String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        return StringEscapeUtils.escapeXml11(input);
    }

    /**
     * Context-aware sanitizer - detects context and applies appropriate escaping
     * Primarily used for HTML contexts
     */
    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        // Default to HTML escaping for web view rendering
        return StringEscapeUtils.escapeHtml4(input);
    }

    /**
     * Strip HTML tags - removes all HTML/XML tags for plain text display
     */
    public static String stripHtmlTags(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("<[^>]*>", "");
    }

    /**
     * Truncate and sanitize - safely truncate text and escape it
     */
    public static String sanitizeAndTruncate(String input, int maxLength) {
        if (input == null) {
            return "";
        }
        String truncated = input.length() > maxLength ? input.substring(0, maxLength) + "..." : input;
        return escapeHtml(truncated);
    }

    /**
     * Validate and sanitize complaint data - comprehensive sanitization for complaint fields
     */
    public static String sanitizeComplaintField(String input, String fieldType) {
        if (input == null) {
            return "";
        }

        String sanitized = input.trim();

        // Size validation
        switch (fieldType) {
            case "title":
                sanitized = sanitizeAndTruncate(sanitized, 255);
                break;
            case "description":
                sanitized = sanitizeAndTruncate(sanitized, 5000);
                break;
            case "category":
                sanitized = sanitizeAndTruncate(sanitized, 100);
                break;
            case "status":
                sanitized = sanitizeAndTruncate(sanitized, 50);
                break;
            case "response":
                sanitized = sanitizeAndTruncate(sanitized, 5000);
                break;
            default:
                sanitized = sanitizeAndTruncate(sanitized, 1000);
        }

        // HTML escape all complaint fields
        return escapeHtml(sanitized);
    }
}
