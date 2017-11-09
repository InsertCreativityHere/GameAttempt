//<vertex>
#version 120

attribute vec3 vertices;
attribute vec2 textureCoords;
varying vec2 VtextureCoords;
uniform mat4 projection;

void main()
{
    VtextureCoords = textureCoords;
    gl_Position = projection * vec4(vertices, 1);
}

//</vertex>
//<fragment>
#version 120

uniform sampler2D sampler;
varying vec2 VtextureCoords;

void main()
{
    gl_FragColor = texture2D(sampler, VtextureCoords);
}
//</fragment>